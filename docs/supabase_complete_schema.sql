-- ============================================================================
-- Supabase Complete Database Schema for ShoppingHelper
-- ============================================================================
-- Version: 2.0
-- Last Updated: 2024-01-17
-- 
-- This file contains the complete PostgreSQL schema for ShoppingHelper including:
-- - Initial schema setup
-- - Migration scripts
-- - RLS policy fixes
-- 
-- Usage:
-- 1. Run this entire script in Supabase SQL Editor for fresh setup
-- 2. Or run specific sections for updates
-- ============================================================================

-- ============================================================================
-- SECTION 1: EXTENSIONS & CLEANUP
-- ============================================================================

-- Enable required PostgreSQL extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";  -- For UUID generation

-- Clean up old columns if they exist (migration from old schema)
ALTER TABLE shops DROP COLUMN IF EXISTS location;

-- ============================================================================
-- SECTION 2: ENUMS
-- ============================================================================

-- Drop existing types if they exist (for clean reinstall)
DROP TYPE IF EXISTS priority_level CASCADE;
DROP TYPE IF EXISTS item_category CASCADE;
DROP TYPE IF EXISTS shop_category CASCADE;

-- Item priority levels
CREATE TYPE priority_level AS ENUM ('HIGH', 'NORMAL', 'LOW');

-- Item categories  
CREATE TYPE item_category AS ENUM (
    'FOOD', 'DRINKS', 'DAILY', 'ELECTRONICS', 'CLOTHING', 
    'HEALTH', 'HOUSEHOLD', 'SPORTS', 'BOOKS', 'OTHER'
);

-- Shop categories
CREATE TYPE shop_category AS ENUM (
    'SUPERMARKET', 'PHARMACY', 'ELECTRONICS', 'CLOTHING',
    'RESTAURANT', 'BOOKSTORE', 'SPORTS', 'OTHER'
);

-- ============================================================================
-- SECTION 3: CORE TABLES
-- ============================================================================

-- User profiles (extends Supabase auth.users)
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT,
    display_name TEXT,
    avatar_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Shopping lists
CREATE TABLE IF NOT EXISTS shopping_lists (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL CHECK (char_length(name) > 0),
    owner_id UUID REFERENCES profiles(id) ON DELETE CASCADE NOT NULL,
    is_active BOOLEAN DEFAULT true,
    is_shared BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Shops with location support (latitude/longitude)
CREATE TABLE IF NOT EXISTS shops (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL CHECK (char_length(name) > 0),
    address TEXT,
    latitude DOUBLE PRECISION,  -- 緯度
    longitude DOUBLE PRECISION, -- 経度
    category shop_category NOT NULL,
    is_favorite BOOLEAN DEFAULT false,
    owner_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Ensure latitude/longitude columns exist (migration support)
ALTER TABLE shops 
ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

-- Shopping items
CREATE TABLE IF NOT EXISTS shopping_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    list_id UUID REFERENCES shopping_lists(id) ON DELETE CASCADE NOT NULL,
    name TEXT NOT NULL CHECK (char_length(name) > 0),
    quantity INTEGER DEFAULT 1 CHECK (quantity > 0),
    unit TEXT,
    price DECIMAL(10,2) CHECK (price >= 0),
    priority priority_level DEFAULT 'NORMAL',
    category item_category DEFAULT 'OTHER',
    shop_id UUID REFERENCES shops(id) ON DELETE SET NULL,
    is_completed BOOLEAN DEFAULT false,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE
);

-- Item templates (frequently purchased items)
CREATE TABLE IF NOT EXISTS item_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL CHECK (char_length(name) > 0),
    quantity INTEGER DEFAULT 1 CHECK (quantity > 0),
    unit TEXT,
    category item_category DEFAULT 'OTHER',
    shop_id UUID REFERENCES shops(id) ON DELETE SET NULL,
    notes TEXT,
    use_count INTEGER DEFAULT 0 CHECK (use_count >= 0),
    last_used_at TIMESTAMP WITH TIME ZONE,
    owner_id UUID REFERENCES profiles(id) ON DELETE CASCADE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Shopping list sharing (many-to-many)
CREATE TABLE IF NOT EXISTS list_shares (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    list_id UUID REFERENCES shopping_lists(id) ON DELETE CASCADE NOT NULL,
    user_id UUID REFERENCES profiles(id) ON DELETE CASCADE NOT NULL,
    permission_level TEXT DEFAULT 'read' CHECK (permission_level IN ('read', 'write', 'admin')),
    shared_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    shared_by UUID REFERENCES profiles(id) ON DELETE SET NULL,
    UNIQUE(list_id, user_id)
);

-- ============================================================================
-- SECTION 4: INDEXES FOR PERFORMANCE
-- ============================================================================

-- Shopping lists indexes
CREATE INDEX IF NOT EXISTS idx_shopping_lists_owner_id ON shopping_lists(owner_id);
CREATE INDEX IF NOT EXISTS idx_shopping_lists_active ON shopping_lists(owner_id, is_active);
CREATE INDEX IF NOT EXISTS idx_shopping_lists_updated_at ON shopping_lists(updated_at);

-- Shopping items indexes
CREATE INDEX IF NOT EXISTS idx_shopping_items_list_id ON shopping_items(list_id);
CREATE INDEX IF NOT EXISTS idx_shopping_items_shop_id ON shopping_items(shop_id);
CREATE INDEX IF NOT EXISTS idx_shopping_items_completed ON shopping_items(list_id, is_completed);
CREATE INDEX IF NOT EXISTS idx_shopping_items_priority ON shopping_items(list_id, priority);
CREATE INDEX IF NOT EXISTS idx_shopping_items_updated_at ON shopping_items(updated_at);

-- Shops indexes  
CREATE INDEX IF NOT EXISTS idx_shops_owner_id ON shops(owner_id);
CREATE INDEX IF NOT EXISTS idx_shops_category ON shops(category);
CREATE INDEX IF NOT EXISTS idx_shops_favorite ON shops(owner_id, is_favorite);
CREATE INDEX IF NOT EXISTS idx_shops_location ON shops(latitude, longitude);

-- Item templates indexes
CREATE INDEX IF NOT EXISTS idx_item_templates_owner_id ON item_templates(owner_id);
CREATE INDEX IF NOT EXISTS idx_item_templates_category ON item_templates(owner_id, category);
CREATE INDEX IF NOT EXISTS idx_item_templates_use_count ON item_templates(owner_id, use_count DESC);

-- List shares indexes
CREATE INDEX IF NOT EXISTS idx_list_shares_user_id ON list_shares(user_id);
CREATE INDEX IF NOT EXISTS idx_list_shares_list_id ON list_shares(list_id);

-- ============================================================================
-- SECTION 5: ROW LEVEL SECURITY (RLS) POLICIES
-- ============================================================================

-- Enable RLS on all tables
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE shopping_lists ENABLE ROW LEVEL SECURITY;
ALTER TABLE shops ENABLE ROW LEVEL SECURITY;
ALTER TABLE shopping_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE item_templates ENABLE ROW LEVEL SECURITY;
ALTER TABLE list_shares ENABLE ROW LEVEL SECURITY;

-- Drop all existing policies (for clean setup)
DROP POLICY IF EXISTS "Users can view their own profile" ON profiles;
DROP POLICY IF EXISTS "Users can update their own profile" ON profiles;
DROP POLICY IF EXISTS "Users can insert their own profile" ON profiles;
DROP POLICY IF EXISTS "Users can view their own lists" ON shopping_lists;
DROP POLICY IF EXISTS "Users can create their own lists" ON shopping_lists;
DROP POLICY IF EXISTS "Users can update their own lists" ON shopping_lists;
DROP POLICY IF EXISTS "Users can delete their own lists" ON shopping_lists;
DROP POLICY IF EXISTS "Users can view shops" ON shops;
DROP POLICY IF EXISTS "Users can create shops" ON shops;
DROP POLICY IF EXISTS "Users can update their own shops" ON shops;
DROP POLICY IF EXISTS "Users can delete their own shops" ON shops;
DROP POLICY IF EXISTS "Users can view items in their lists" ON shopping_items;
DROP POLICY IF EXISTS "Users can create items in their lists" ON shopping_items;
DROP POLICY IF EXISTS "Users can update items in their lists" ON shopping_items;
DROP POLICY IF EXISTS "Users can delete items in their lists" ON shopping_items;
DROP POLICY IF EXISTS "Users can view their own templates" ON item_templates;
DROP POLICY IF EXISTS "Users can create their own templates" ON item_templates;
DROP POLICY IF EXISTS "Users can update their own templates" ON item_templates;
DROP POLICY IF EXISTS "Users can delete their own templates" ON item_templates;
DROP POLICY IF EXISTS "Users can view their shares" ON list_shares;
DROP POLICY IF EXISTS "Users can view shares of their lists" ON list_shares;
DROP POLICY IF EXISTS "List owners can manage shares" ON list_shares;
DROP POLICY IF EXISTS "List owners can update shares" ON list_shares;
DROP POLICY IF EXISTS "List owners can delete shares" ON list_shares;

-- Profiles policies
CREATE POLICY "Users can view their own profile" ON profiles
    FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Users can update their own profile" ON profiles
    FOR UPDATE USING (auth.uid() = id);

CREATE POLICY "Users can insert their own profile" ON profiles
    FOR INSERT WITH CHECK (auth.uid() = id);

-- Shopping lists policies (simplified to avoid recursion)
CREATE POLICY "Users can view their own lists" ON shopping_lists
    FOR SELECT USING (auth.uid() = owner_id);

CREATE POLICY "Users can create their own lists" ON shopping_lists
    FOR INSERT WITH CHECK (auth.uid() = owner_id);

CREATE POLICY "Users can update their own lists" ON shopping_lists
    FOR UPDATE USING (auth.uid() = owner_id);

CREATE POLICY "Users can delete their own lists" ON shopping_lists
    FOR DELETE USING (auth.uid() = owner_id);

-- Shops policies  
CREATE POLICY "Users can view shops" ON shops
    FOR SELECT USING (
        owner_id IS NULL  -- Public shops
        OR auth.uid() = owner_id  -- Own shops
    );

CREATE POLICY "Users can create shops" ON shops
    FOR INSERT WITH CHECK (auth.uid() = owner_id OR owner_id IS NULL);

CREATE POLICY "Users can update their own shops" ON shops
    FOR UPDATE USING (auth.uid() = owner_id);

CREATE POLICY "Users can delete their own shops" ON shops
    FOR DELETE USING (auth.uid() = owner_id);

-- Shopping items policies (simplified to avoid recursion)
CREATE POLICY "Users can view items in their lists" ON shopping_items
    FOR SELECT USING (
        list_id IN (
            SELECT id FROM shopping_lists 
            WHERE owner_id = auth.uid()
        )
    );

CREATE POLICY "Users can create items in their lists" ON shopping_items
    FOR INSERT WITH CHECK (
        list_id IN (
            SELECT id FROM shopping_lists 
            WHERE owner_id = auth.uid()
        )
    );

CREATE POLICY "Users can update items in their lists" ON shopping_items
    FOR UPDATE USING (
        list_id IN (
            SELECT id FROM shopping_lists 
            WHERE owner_id = auth.uid()
        )
    );

CREATE POLICY "Users can delete items in their lists" ON shopping_items
    FOR DELETE USING (
        list_id IN (
            SELECT id FROM shopping_lists 
            WHERE owner_id = auth.uid()
        )
    );

-- Item templates policies
CREATE POLICY "Users can view their own templates" ON item_templates
    FOR SELECT USING (auth.uid() = owner_id);

CREATE POLICY "Users can create their own templates" ON item_templates
    FOR INSERT WITH CHECK (auth.uid() = owner_id);

CREATE POLICY "Users can update their own templates" ON item_templates
    FOR UPDATE USING (auth.uid() = owner_id);

CREATE POLICY "Users can delete their own templates" ON item_templates
    FOR DELETE USING (auth.uid() = owner_id);

-- List shares policies (simplified to avoid recursion)
CREATE POLICY "Users can view their shares" ON list_shares
    FOR SELECT USING (user_id = auth.uid());

CREATE POLICY "Users can view shares of their lists" ON list_shares
    FOR SELECT USING (
        list_id IN (
            SELECT id FROM shopping_lists 
            WHERE owner_id = auth.uid()
        )
    );

CREATE POLICY "List owners can manage shares" ON list_shares
    FOR INSERT WITH CHECK (
        list_id IN (
            SELECT id FROM shopping_lists 
            WHERE owner_id = auth.uid()
        )
    );

CREATE POLICY "List owners can update shares" ON list_shares
    FOR UPDATE USING (
        list_id IN (
            SELECT id FROM shopping_lists 
            WHERE owner_id = auth.uid()
        )
    );

CREATE POLICY "List owners can delete shares" ON list_shares
    FOR DELETE USING (
        list_id IN (
            SELECT id FROM shopping_lists 
            WHERE owner_id = auth.uid()
        )
    );

-- ============================================================================
-- SECTION 6: TRIGGERS FOR AUTOMATIC TIMESTAMPS
-- ============================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers to relevant tables
DROP TRIGGER IF EXISTS update_profiles_updated_at ON profiles;
CREATE TRIGGER update_profiles_updated_at 
    BEFORE UPDATE ON profiles 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_shopping_lists_updated_at ON shopping_lists;
CREATE TRIGGER update_shopping_lists_updated_at 
    BEFORE UPDATE ON shopping_lists 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_shops_updated_at ON shops;
CREATE TRIGGER update_shops_updated_at 
    BEFORE UPDATE ON shops 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_shopping_items_updated_at ON shopping_items;
CREATE TRIGGER update_shopping_items_updated_at 
    BEFORE UPDATE ON shopping_items 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- SECTION 7: HELPER FUNCTIONS
-- ============================================================================

-- Function to create user profile on signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, email, display_name)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'display_name', split_part(NEW.email, '@', 1))
    )
    ON CONFLICT (id) DO NOTHING;
    RETURN NEW;
END;
$$ language 'plpgsql' SECURITY DEFINER;

-- Trigger to automatically create profile on user signup
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- Function to get shops with pending items for geofencing
CREATE OR REPLACE FUNCTION get_shops_with_pending_items(user_uuid UUID)
RETURNS TABLE (
    shop_id UUID,
    shop_name TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    pending_items_count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        s.id as shop_id,
        s.name as shop_name,
        s.latitude,
        s.longitude,
        COUNT(si.id) as pending_items_count
    FROM shops s
    INNER JOIN shopping_items si ON s.id = si.shop_id
    INNER JOIN shopping_lists sl ON si.list_id = sl.id
    WHERE sl.owner_id = user_uuid
        AND si.is_completed = false
        AND sl.is_active = true
        AND s.latitude IS NOT NULL
        AND s.longitude IS NOT NULL
    GROUP BY s.id, s.name, s.latitude, s.longitude
    HAVING COUNT(si.id) > 0;
END;
$$ language 'plpgsql' SECURITY DEFINER;

-- ============================================================================
-- SECTION 8: VERIFICATION QUERIES
-- ============================================================================

-- Verify table structure
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable
FROM 
    information_schema.columns
WHERE 
    table_schema = 'public'
    AND table_name IN ('profiles', 'shopping_lists', 'shops', 'shopping_items', 'item_templates', 'list_shares')
ORDER BY 
    table_name, ordinal_position;

-- Verify RLS policies
SELECT 
    schemaname,
    tablename,
    policyname,
    permissive,
    roles,
    cmd,
    qual
FROM 
    pg_policies 
WHERE 
    tablename IN ('profiles', 'shopping_lists', 'shops', 'shopping_items', 'item_templates', 'list_shares')
ORDER BY 
    tablename, policyname;

-- Verify indexes
SELECT 
    tablename,
    indexname,
    indexdef
FROM 
    pg_indexes 
WHERE 
    schemaname = 'public'
    AND tablename IN ('profiles', 'shopping_lists', 'shops', 'shopping_items', 'item_templates', 'list_shares')
ORDER BY 
    tablename, indexname;

-- ============================================================================
-- SECTION 9: SAMPLE DATA (OPTIONAL - FOR DEVELOPMENT)
-- ============================================================================

-- Uncomment to insert sample data for development
/*
-- Sample shop categories (public shops without owner)
INSERT INTO shops (name, address, latitude, longitude, category, owner_id) VALUES
('Sample Supermarket', '123 Main St', 35.6895, 139.6917, 'SUPERMARKET', null),
('Corner Pharmacy', '456 Oak Ave', 35.7000, 139.7000, 'PHARMACY', null),
('Electronics Store', '789 Tech Blvd', 35.7100, 139.7100, 'ELECTRONICS', null)
ON CONFLICT DO NOTHING;
*/

-- ============================================================================
-- COMPLETE - Your ShoppingHelper database is ready!
-- ============================================================================
-- Next steps:
-- 1. Configure your Supabase API keys in the app
-- 2. Test the connection using the debug menu
-- 3. Start using the app!
-- ============================================================================