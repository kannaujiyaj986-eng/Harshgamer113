package com.example.data.repository

import com.example.data.local.BlockedUserEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.HarshgamerDao
import com.example.data.local.HelpQuestionEntity
import com.example.data.local.ReportedMessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class Message(
    val id: String = "",
    val roomId: String = "global",
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: String = "SQUAD MEMBER",
    val text: String = "",
    val isFromCurrentUser: Boolean = false,
    val createdAt: Long = 0L
)

class ChatRepository(
    private val dao: HarshgamerDao,
    private val currentUserProvider: () -> LocalGamerUser?
) {
    val myUid: String
        get() = currentUserProvider()?.email ?: "guest@harshgamer.app"

    val myName: String
        get() = currentUserProvider()?.displayName ?: "User"

    fun messages(roomId: String = "global"): Flow<List<Message>> {
        return dao.observeChatMessages(roomId).map { entities ->
            entities.map { entity ->
                Message(
                    id = entity.id.toString(),
                    roomId = entity.roomId,
                    senderId = entity.senderId,
                    senderName = entity.senderName,
                    senderRole = entity.senderRole,
                    text = entity.message,
                    isFromCurrentUser = entity.isFromCurrentUser || entity.senderId == myUid,
                    createdAt = entity.sentAt
                )
            }
        }
    }

    suspend fun send(text: String, roomId: String = "global") {
        val clean = text.trim()
        if (clean.isBlank() || clean.length > 1000) return
        dao.insertChatMessage(
            ChatMessageEntity(
                roomId = roomId,
                senderId = myUid,
                senderName = myName,
                senderRole = if (myUid == "squad113@harshgamer.app") "CREATOR" else "SQUAD MEMBER",
                message = clean,
                isFromCurrentUser = true,
                sentAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun reportMessage(msg: Message, reason: String) {
        val cleanReason = reason.trim().ifBlank { "Inappropriate content" }
        dao.insertReport(
            ReportedMessageEntity(
                messageId = msg.id,
                reportedUid = msg.senderId,
                reportedText = msg.text,
                reason = cleanReason,
                reporterUid = myUid,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun blockUser(uid: String, displayName: String = "") {
        if (uid.isBlank() || uid == myUid) return
        dao.blockUser(
            BlockedUserEntity(
                ownerUid = myUid,
                blockedUid = uid,
                blockedName = displayName.ifBlank { uid },
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun unblockUser(uid: String) {
        dao.unblockUser(ownerUid = myUid, blockedUid = uid)
    }

    fun observeBlockedUsers(): Flow<List<BlockedUserEntity>> = dao.observeBlockedUsers(myUid)

    suspend fun blockedList(): Set<String> {
        return dao.getBlockedUidsOnce(myUid).toSet()
    }

    suspend fun askQuestion(question: String) {
        val clean = question.trim()
        if (clean.isBlank()) return
        dao.insertHelpQuestion(
            HelpQuestionEntity(
                uid = myUid,
                name = myName,
                question = clean,
                answer = "Received by Squad 113 Support — check FAQs above or join our @harshgamer-113 community stream for live help.",
                createdAt = System.currentTimeMillis()
            )
        )
    }

    fun observeMyQuestions(): Flow<List<HelpQuestionEntity>> = dao.observeHelpQuestions(myUid)

    suspend fun myQuestions(): List<Map<String, Any>> {
        return dao.getHelpQuestionsOnce(myUid).map { item ->
            mapOf(
                "id" to item.id,
                "uid" to item.uid,
                "name" to item.name,
                "question" to item.question,
                "answer" to item.answer,
                "createdAt" to item.createdAt
            )
        }
    }
}
