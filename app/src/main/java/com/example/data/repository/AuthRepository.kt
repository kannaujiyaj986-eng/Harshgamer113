package com.example.data.repository

import android.content.Context
import com.example.data.local.HarshgamerDao
import com.example.data.local.PlayerProfileEntity
import com.example.data.local.UserAccountEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class LocalGamerUser(
    val displayName: String,
    val email: String,
    val username: String = displayName.lowercase().replace(Regex("[^a-z0-9_]"), "")
)

class AuthRepository(
    context: Context,
    private val dao: HarshgamerDao
) {
    private val prefs = context.applicationContext.getSharedPreferences("hg113_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow(loadSavedSession())
    val currentUser: StateFlow<LocalGamerUser?> = _currentUser.asStateFlow()

    private val _rememberLogin = MutableStateFlow(prefs.getBoolean(KEY_REMEMBER, true))
    val rememberLogin: StateFlow<Boolean> = _rememberLogin.asStateFlow()

    val isLoggedIn: Boolean
        get() = _currentUser.value != null

    private fun loadSavedSession(): LocalGamerUser? {
        if (!prefs.getBoolean(KEY_REMEMBER, true)) return null
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        val name = prefs.getString(KEY_NAME, null) ?: return null
        val username = prefs.getString(KEY_USERNAME, null) ?: name.lowercase().replace(Regex("[^a-z0-9_]"), "")
        return LocalGamerUser(displayName = name, email = email, username = username)
    }

    private fun saveSession(user: LocalGamerUser, remember: Boolean) {
        _rememberLogin.value = remember
        prefs.edit().putBoolean(KEY_REMEMBER, remember).apply()
        if (remember) {
            prefs.edit()
                .putString(KEY_EMAIL, user.email)
                .putString(KEY_NAME, user.displayName)
                .putString(KEY_USERNAME, user.username)
                .apply()
        } else {
            prefs.edit().remove(KEY_EMAIL).remove(KEY_NAME).remove(KEY_USERNAME).apply()
        }
        _currentUser.value = user
    }

    fun setRememberLogin(enabled: Boolean) {
        _rememberLogin.value = enabled
        val user = _currentUser.value
        if (enabled && user != null) {
            saveSession(user, true)
        } else {
            prefs.edit()
                .putBoolean(KEY_REMEMBER, enabled)
                .remove(KEY_EMAIL)
                .remove(KEY_NAME)
                .remove(KEY_USERNAME)
                .apply()
        }
    }

    private fun generateSaltHex(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hashPasswordPbkdf2(raw: String, saltHex: String): String {
        return try {
            val spec = PBEKeySpec(raw.toCharArray(), saltHex.toByteArray(), 10_000, 256)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            factory.generateSecret(spec).encoded.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            sha256("$saltHex:$raw")
        }
    }

    private fun sha256(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun verifyPassword(raw: String, account: UserAccountEntity): Boolean {
        return if (account.passwordSalt.isNotBlank()) {
            hashPasswordPbkdf2(raw, account.passwordSalt) == account.passwordHash
        } else {
            sha256(raw) == account.passwordHash
        }
    }

    suspend fun ensureSeedCreatorAccount() {
        val creatorEmail = "squad113@harshgamer.app"
        if (dao.findUserByEmail(creatorEmail) == null) {
            val salt = generateSaltHex()
            dao.upsertUserAccount(
                UserAccountEntity(
                    email = creatorEmail,
                    displayName = "Harshgamer113",
                    username = "harshgamer113",
                    passwordHash = hashPasswordPbkdf2("gamer113", salt),
                    passwordSalt = salt
                )
            )
        }
    }

    suspend fun login(
        email: String,
        password: String,
        rememberMe: Boolean = true
    ): Result<LocalGamerUser> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }
        ensureSeedCreatorAccount()
        val existing = dao.findUserByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("Account not found. Please register first."))
        if (!verifyPassword(password, existing)) {
            return Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
        }
        val cleanUsername = existing.username.ifBlank {
            existing.displayName.lowercase().replace(Regex("[^a-z0-9_]"), "").ifBlank { "player113" }
        }
        ensureProfileForUser(cleanEmail, existing.displayName, cleanUsername)
        val user = LocalGamerUser(
            displayName = existing.displayName,
            email = existing.email,
            username = cleanUsername
        )
        saveSession(user, rememberMe)
        return Result.success(user)
    }

    suspend fun signup(
        displayName: String,
        email: String,
        password: String,
        username: String = "",
        rememberMe: Boolean = true
    ): Result<LocalGamerUser> {
        val cleanName = displayName.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanUsername = username.trim().lowercase()
            .replace(Regex("[^a-z0-9_]"), "")
            .ifBlank { cleanName.lowercase().replace(Regex("[^a-z0-9_]"), "").ifBlank { "gamer113" } }

        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("Full name / Gamer name is required."))
        }
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }
        if (dao.findUserByEmail(cleanEmail) != null) {
            return Result.failure(IllegalArgumentException("An account with this email already exists."))
        }
        val salt = generateSaltHex()
        val account = UserAccountEntity(
            email = cleanEmail,
            displayName = cleanName,
            username = cleanUsername,
            passwordHash = hashPasswordPbkdf2(password, salt),
            passwordSalt = salt
        )
        dao.upsertUserAccount(account)
        ensureProfileForUser(cleanEmail, cleanName, cleanUsername)
        val user = LocalGamerUser(displayName = cleanName, email = cleanEmail, username = cleanUsername)
        saveSession(user, rememberMe)
        return Result.success(user)
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<String> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Enter your registered email address."))
        }
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("New password must be at least 6 characters."))
        }
        ensureSeedCreatorAccount()
        val existing = dao.findUserByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("No account found for $cleanEmail."))
        val newSalt = generateSaltHex()
        dao.upsertUserAccount(
            existing.copy(
                passwordHash = hashPasswordPbkdf2(newPassword, newSalt),
                passwordSalt = newSalt
            )
        )
        return Result.success("Password reset successfully for $cleanEmail. You can now log in.")
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<String> {
        val active = _currentUser.value
            ?: return Result.failure(IllegalStateException("You must be logged in to change password."))
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("New password must be at least 6 characters."))
        }
        val existing = dao.findUserByEmail(active.email)
            ?: return Result.failure(IllegalStateException("Account record not found."))
        if (!verifyPassword(currentPassword, existing)) {
            return Result.failure(IllegalArgumentException("Current password is incorrect."))
        }
        val newSalt = generateSaltHex()
        dao.upsertUserAccount(
            existing.copy(
                passwordHash = hashPasswordPbkdf2(newPassword, newSalt),
                passwordSalt = newSalt
            )
        )
        return Result.success("Password updated securely.")
    }

    private suspend fun ensureProfileForUser(email: String, fullName: String, username: String) {
        if (dao.getPlayerProfileOnce(email) == null) {
            dao.upsertPlayerProfile(
                PlayerProfileEntity(
                    ownerEmail = email,
                    username = username,
                    fullName = fullName,
                    gamerTag = fullName,
                    mainGame = "BGMI",
                    role = "Assaulter",
                    deviceName = "120FPS Pro OLED",
                    targetKd = "5.0 K/D",
                    bio = "Squad 113 member on Harshgamer113 Hub."
                )
            )
        }
    }

    fun updateActiveUserDisplay(displayName: String, username: String) {
        val active = _currentUser.value ?: return
        val updated = active.copy(displayName = displayName, username = username)
        _currentUser.value = updated
        if (_rememberLogin.value) {
            prefs.edit()
                .putString(KEY_NAME, updated.displayName)
                .putString(KEY_USERNAME, updated.username)
                .apply()
        }
    }

    fun logout() {
        prefs.edit().remove(KEY_EMAIL).remove(KEY_NAME).remove(KEY_USERNAME).apply()
        _currentUser.value = null
    }

    companion object {
        private const val KEY_EMAIL = "session_email"
        private const val KEY_NAME = "session_name"
        private const val KEY_USERNAME = "session_username"
        private const val KEY_REMEMBER = "remember_login"
    }
}
