package ru.citadel.rise.data.local

import androidx.room.*
import ru.citadel.rise.data.model.*

@Dao
interface ILocalDao {
    // users dao
    @Query("SELECT * FROM users")
    fun getAllUsers(): List<User>

    @Query("SELECT * FROM users WHERE userId=:userId")
    fun getUserById(userId: Int): User

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertUser(user: User)

    @Update
    fun updateUser(user: User)

    @Delete
    fun deleteUser(user: User)

    // chats dao
    @Query("SELECT * FROM chats_short WHERE userId=:userId")
    fun getAllUserChats(userId: Int): List<ChatShortView>

    @Query("SELECT chatId FROM chats_short WHERE userId=:userFrom AND toId=:userTo")
    fun getChatIdByUsers(userFrom: Int, userTo: Int): Int?

    @Update
    fun updateChat(chat: ChatShortView)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChat(chat: List<ChatShortView>)

    @Delete
    fun deleteChat(chat: List<ChatShortView>)


    // projects dao
    @Query("SELECT * FROM projects")
    fun getAllProjects(): List<Project>

    @Query("SELECT projectId, name, contact, contactName, descriptionLong, cost, deadlines, website, tags FROM projects JOIN user_with_their_projects ON projects.projectId=user_with_their_projects.projId WHERE user_with_their_projects.userId=:userId")
    fun getUserTheirProjects(userId: Int): List<Project>

    @Query("SELECT projectId, name, contact, contactName, descriptionLong, cost, deadlines, website, tags FROM projects JOIN user_with_fav_projects ON projects.projectId=user_with_fav_projects.projId WHERE user_with_fav_projects.userId=:userId")
    fun getUserFavProjects(userId: Int): List<Project>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProjects(projects: List<Project>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertUserWithTheirProject(userWithTheirProject: UserWithTheirProjects)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertUserWithFavProject(userWithFavProject: UserWithFavProjects)

    @Update
    fun updateProject(project: Project)

    @Delete
    fun deleteProjects(projects: List<Project>)

    @Delete
    fun deleteUserWithTheirProject(userWithTheirProject: UserWithTheirProjects)

    @Delete
    fun deleteUserWithFavProject(userWithFavProject: UserWithFavProjects)

}