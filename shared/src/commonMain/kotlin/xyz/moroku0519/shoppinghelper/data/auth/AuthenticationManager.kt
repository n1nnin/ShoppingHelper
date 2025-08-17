package xyz.moroku0519.shoppinghelper.data.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Authentication manager for ShoppingHelper using Supabase Auth
 * 
 * Handles user authentication including:
 * - Email/password sign up and sign in
 * - Session management
 * - User state monitoring
 */
class AuthenticationManager(
    private val supabaseClient: SupabaseClient
) {
    private val auth: Auth = supabaseClient.auth
    
    /**
     * Current user as a Flow for reactive UI updates
     */
    val currentUser: Flow<UserInfo?> = auth.sessionStatus.map { sessionStatus ->
        when (sessionStatus) {
            is io.github.jan.supabase.gotrue.SessionStatus.Authenticated -> sessionStatus.session.user
            else -> null
        }
    }
    
    /**
     * Authentication state as a Flow
     */
    val isAuthenticated: Flow<Boolean> = auth.sessionStatus.map { sessionStatus ->
        sessionStatus is io.github.jan.supabase.gotrue.SessionStatus.Authenticated
    }
    
    /**
     * Sign up with email and password
     * 
     * @param email User's email address
     * @param password User's password (minimum 6 characters)
     * @return Result with user info or error
     */
    suspend fun signUpWithEmail(email: String, password: String): Result<UserInfo> {
        return try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            val user = auth.currentUserOrNull()
            user?.let { 
                Result.success(it) 
            } ?: Result.failure(Exception("Sign up failed: No user returned"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sign in with email and password
     * 
     * @param email User's email address
     * @param password User's password
     * @return Result with user info or error
     */
    suspend fun signInWithEmail(email: String, password: String): Result<UserInfo> {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val user = auth.currentUserOrNull()
            user?.let { 
                Result.success(it) 
            } ?: Result.failure(Exception("Sign in failed: No user found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sign out the current user
     * 
     * @return Result indicating success or failure
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current user synchronously
     * 
     * @return Current user info or null if not authenticated
     */
    fun getCurrentUser(): UserInfo? {
        return auth.currentUserOrNull()
    }
    
    /**
     * Check if user is currently authenticated
     * 
     * @return true if user is authenticated, false otherwise
     */
    fun isUserAuthenticated(): Boolean {
        return auth.currentUserOrNull() != null
    }
    
    /**
     * Get current user ID
     * 
     * @return User ID string or null if not authenticated
     */
    fun getCurrentUserId(): String? {
        return auth.currentUserOrNull()?.id
    }
    
    /**
     * Refresh current session
     * 
     * @return Result indicating success or failure
     */
    suspend fun refreshSession(): Result<Unit> {
        return try {
            auth.refreshCurrentSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}