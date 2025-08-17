-- ============================================================================
-- Supabase Database Schema for ShoppingHelper
-- ============================================================================
-- This file contains the complete PostgreSQL schema for ShoppingHelper
-- Run this in Supabase SQL Editor to set up the database
-- 
-- Features:
-- - User profiles linked to Supabase Auth
-- - Shopping lists with sharing capabilities  
-- - Items with priority and categorization
-- - Shops with location data
-- - Item templates for frequent purchases
-- - Row Level Security (RLS) for data isolation
-- - Real-time subscriptions support
-- ============================================================================

-- ============================================================================
-- 1. EXTENSIONS
-- ============================================================================
-- Enable required PostgreSQL extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";  -- For UUID generation
CREATE EXTENSION IF NOT EXISTS "postgis";    -- For location/geography support

-- ============================================================================
-- 2. ENUMS
-- ============================================================================
-- Define custom types for consistency

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
-- 3. CORE TABLES
-- ============================================================================

-- User profiles (extends Supabase auth.users)
CREATE TABLE profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT,
    display_name TEXT,
    avatar_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Shopping lists
CREATE TABLE shopping_lists (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL CHECK (char_length(name) > 0),
    owner_id UUID REFERENCES profiles(id) ON DELETE CASCADE NOT NULL,
    is_active BOOLEAN DEFAULT true,
    is_shared BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Shops with location support
CREATE TABLE shops (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL CHECK (char_length(name) > 0),
    address TEXT,
    location GEOGRAPHY(POINT),  -- PostGIS point for lat/lng
    category shop_category NOT NULL,
    is_favorite BOOLEAN DEFAULT false,
    owner_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Shopping items
CREATE TABLE shopping_items (
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
CREATE TABLE item_templates (
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
CREATE TABLE list_shares (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    list_id UUID REFERENCES shopping_lists(id) ON DELETE CASCADE NOT NULL,
    user_id UUID REFERENCES profiles(id) ON DELETE CASCADE NOT NULL,
    permission_level TEXT DEFAULT 'read' CHECK (permission_level IN ('read', 'write', 'admin')),
    shared_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    shared_by UUID REFERENCES profiles(id) ON DELETE SET NULL,
    UNIQUE(list_id, user_id)
);

-- ============================================================================
-- 4. INDEXES FOR PERFORMANCE
-- ============================================================================

-- Shopping lists indexes
CREATE INDEX idx_shopping_lists_owner_id ON shopping_lists(owner_id);
CREATE INDEX idx_shopping_lists_active ON shopping_lists(owner_id, is_active);
CREATE INDEX idx_shopping_lists_updated_at ON shopping_lists(updated_at);

-- Shopping items indexes
CREATE INDEX idx_shopping_items_list_id ON shopping_items(list_id);
CREATE INDEX idx_shopping_items_shop_id ON shopping_items(shop_id);
CREATE INDEX idx_shopping_items_completed ON shopping_items(list_id, is_completed);
CREATE INDEX idx_shopping_items_priority ON shopping_items(list_id, priority);
CREATE INDEX idx_shopping_items_updated_at ON shopping_items(updated_at);

-- Shops indexes  
CREATE INDEX idx_shops_owner_id ON shops(owner_id);
CREATE INDEX idx_shops_category ON shops(category);
CREATE INDEX idx_shops_favorite ON shops(owner_id, is_favorite);
CREATE INDEX idx_shops_location ON shops USING GIST(location);  -- Spatial index

-- Item templates indexes
CREATE INDEX idx_item_templates_owner_id ON item_templates(owner_id);
CREATE INDEX idx_item_templates_category ON item_templates(owner_id, category);
CREATE INDEX idx_item_templates_use_count ON item_templates(owner_id, use_count DESC);

-- List shares indexes
CREATE INDEX idx_list_shares_user_id ON list_shares(user_id);
CREATE INDEX idx_list_shares_list_id ON list_shares(list_id);

-- ============================================================================
-- 5. ROW LEVEL SECURITY (RLS) POLICIES
-- ============================================================================

-- Enable RLS on all tables
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE shopping_lists ENABLE ROW LEVEL SECURITY;
ALTER TABLE shops ENABLE ROW LEVEL SECURITY;
ALTER TABLE shopping_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE item_templates ENABLE ROW LEVEL SECURITY;
ALTER TABLE list_shares ENABLE ROW LEVEL SECURITY;

-- Profiles policies (users can only see/edit their own profile)
CREATE POLICY "Users can view their own profile" ON profiles
    FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Users can update their own profile" ON profiles
    FOR UPDATE USING (auth.uid() = id);

CREATE POLICY "Users can insert their own profile" ON profiles
    FOR INSERT WITH CHECK (auth.uid() = id);

-- Shopping lists policies
CREATE POLICY "Users can view their own lists" ON shopping_lists
    FOR SELECT USING (
        auth.uid() = owner_id 
        OR id IN (
            SELECT list_id FROM list_shares 
            WHERE user_id = auth.uid()
        )
    );

CREATE POLICY "Users can create their own lists" ON shopping_lists
    FOR INSERT WITH CHECK (auth.uid() = owner_id);

CREATE POLICY "Users can update their own lists" ON shopping_lists
    FOR UPDATE USING (
        auth.uid() = owner_id 
        OR id IN (
            SELECT list_id FROM list_shares 
            WHERE user_id = auth.uid() 
            AND permission_level IN ('write', 'admin')
        )
    );

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

-- Shopping items policies
CREATE POLICY "Users can view items in accessible lists" ON shopping_items
    FOR SELECT USING (
        list_id IN (
            SELECT id FROM shopping_lists 
            WHERE owner_id = auth.uid()
            OR id IN (
                SELECT list_id FROM list_shares 
                WHERE user_id = auth.uid()
            )
        )
    );

CREATE POLICY "Users can create items in accessible lists" ON shopping_items
    FOR INSERT WITH CHECK (
        list_id IN (
            SELECT id FROM shopping_lists 
            WHERE owner_id = auth.uid()
            OR id IN (
                SELECT list_id FROM list_shares 
                WHERE user_id = auth.uid() 
                AND permission_level IN ('write', 'admin')
            )
        )
    );

CREATE POLICY "Users can update items in accessible lists" ON shopping_items
    FOR UPDATE USING (
        list_id IN (
            SELECT id FROM shopping_lists 
            WHERE owner_id = auth.uid()
            OR id IN (
                SELECT list_id FROM list_shares 
                WHERE user_id = auth.uid() 
                AND permission_level IN ('write', 'admin')
            )
        )
    );

CREATE POLICY "Users can delete items in accessible lists" ON shopping_items
    FOR DELETE USING (
        list_id IN (
            SELECT id FROM shopping_lists 
            WHERE owner_id = auth.uid()
            OR id IN (
                SELECT list_id FROM list_shares 
                WHERE user_id = auth.uid() 
                AND permission_level IN ('write', 'admin')
            )
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

-- List shares policies
CREATE POLICY "Users can view shares for their lists" ON list_shares
    FOR SELECT USING (
        user_id = auth.uid()  -- Shares with me
        OR list_id IN (
            SELECT id FROM shopping_lists 
            WHERE owner_id = auth.uid()  -- Shares from my lists
        )
    );

CREATE POLICY "List owners can manage shares" ON list_shares
    FOR ALL USING (
        list_id IN (
            SELECT id FROM shopping_lists 
            WHERE owner_id = auth.uid()
        )
    );

-- ============================================================================
-- 6. TRIGGERS FOR AUTOMATIC TIMESTAMPS
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
CREATE TRIGGER update_profiles_updated_at 
    BEFORE UPDATE ON profiles 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_shopping_lists_updated_at 
    BEFORE UPDATE ON shopping_lists 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_shops_updated_at 
    BEFORE UPDATE ON shops 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_shopping_items_updated_at 
    BEFORE UPDATE ON shopping_items 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 7. HELPER FUNCTIONS
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
    );
    RETURN NEW;
END;
$$ language 'plpgsql' SECURITY DEFINER;

-- Trigger to automatically create profile on user signup
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
        ST_Y(s.location::geometry) as latitude,
        ST_X(s.location::geometry) as longitude,
        COUNT(si.id) as pending_items_count
    FROM shops s
    INNER JOIN shopping_items si ON s.id = si.shop_id
    INNER JOIN shopping_lists sl ON si.list_id = sl.id
    WHERE sl.owner_id = user_uuid
        AND si.is_completed = false
        AND sl.is_active = true
        AND s.location IS NOT NULL
    GROUP BY s.id, s.name, s.location
    HAVING COUNT(si.id) > 0;
END;
$$ language 'plpgsql' SECURITY DEFINER;

-- ============================================================================
-- 8. SAMPLE DATA (OPTIONAL - FOR DEVELOPMENT)
-- ============================================================================

-- Uncomment to insert sample data for development
/*
-- Sample shop categories
INSERT INTO shops (id, name, address, location, category, owner_id) VALUES
(uuid_generate_v4(), 'Sample Supermarket', '123 Main St', ST_SetSRID(ST_MakePoint(139.6917, 35.6895), 4326), 'SUPERMARKET', null),
(uuid_generate_v4(), 'Corner Pharmacy', '456 Oak Ave', ST_SetSRID(ST_MakePoint(139.7000, 35.7000), 4326), 'PHARMACY', null),
(uuid_generate_v4(), 'Electronics Store', '789 Tech Blvd', ST_SetSRID(ST_MakePoint(139.7100, 35.7100), 4326), 'ELECTRONICS', null);
*/

-- ============================================================================
-- SCHEMA SETUP COMPLETE
-- ============================================================================
-- After running this script:
-- 1. Verify all tables are created
-- 2. Test RLS policies with different users
-- 3. Set up real-time subscriptions in your app
-- 4. Configure your Supabase API keys in local.properties
-- ============================================================================