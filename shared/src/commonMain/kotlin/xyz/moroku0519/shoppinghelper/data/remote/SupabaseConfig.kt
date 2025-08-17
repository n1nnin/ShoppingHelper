package xyz.moroku0519.shoppinghelper.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

/**
 * Supabase client configuration for ShoppingHelper
 * 
 * This object provides a configured Supabase client instance with:
 * - Auth: User authentication (email/password, OAuth)
 * - Postgrest: Database operations with Row Level Security
 * - Realtime: Live data synchronization
 */
object SupabaseConfig {
    
    /**
     * Creates and configures the Supabase client
     * 
     * @param supabaseUrl Project URL from Supabase dashboard
     * @param supabasePublishableKey Publishable key from Supabase dashboard (recommended over legacy anon key)
     * @return Configured SupabaseClient instance
     */
    fun createClient(supabaseUrl: String, supabasePublishableKey: String): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabasePublishableKey
        ) {
            install(Auth) {
                // Store session in platform-specific storage
                // autoRefreshToken and autoSaveToStorage are default in 2.0.0
            }
            
            install(Postgrest)
            
            install(Realtime) {
                // Enable real-time subscriptions
                // Useful for collaborative shopping lists
            }
        }
    }
}

/**
 * Default Supabase configuration keys
 * These should be stored in local.properties for security
 */
object SupabaseConstants {
    const val URL_KEY = "SUPABASE_URL"
    const val PUBLISHABLE_KEY = "SUPABASE_PUBLISHABLE_KEY"
    
    // Legacy key name (deprecated, use PUBLISHABLE_KEY instead)
    @Deprecated("Use PUBLISHABLE_KEY instead. anon key is legacy.", ReplaceWith("PUBLISHABLE_KEY"))
    const val ANON_KEY = "SUPABASE_ANON_KEY"
    
    // Default placeholder values
    const val DEFAULT_URL = "https://your-project.supabase.co"
    const val DEFAULT_PUBLISHABLE_KEY = "your-publishable-key-here"
    
    @Deprecated("Use DEFAULT_PUBLISHABLE_KEY instead")
    const val DEFAULT_ANON_KEY = "your-anon-key-here"
}