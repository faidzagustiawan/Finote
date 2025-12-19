-- SQL Script untuk Setup Database Supabase
-- Gunakan ini di Supabase SQL Editor untuk membuat tabel dan struktur

-- ============================================
-- 1. CREATE TABLE TRANSACTIONS
-- ============================================

CREATE TABLE IF NOT EXISTS transactions (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    title text NOT NULL,
    amount bigint NOT NULL,
    is_income boolean NOT NULL DEFAULT false,
    category text NOT NULL,
    date timestamp with time zone NOT NULL,
    notes text,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now()
);

-- ============================================
-- 2. CREATE INDEXES untuk Performance
-- ============================================

-- Index untuk query berdasarkan user_id
CREATE INDEX IF NOT EXISTS idx_transactions_user_id
ON transactions(user_id);

-- Index untuk query berdasarkan date (descending)
CREATE INDEX IF NOT EXISTS idx_transactions_date
ON transactions(date DESC);

-- Index untuk query berdasarkan category
CREATE INDEX IF NOT EXISTS idx_transactions_category
ON transactions(category);

-- Index kombinasi user_id dan date
CREATE INDEX IF NOT EXISTS idx_transactions_user_date
ON transactions(user_id, date DESC);

-- Index kombinasi user_id dan category
CREATE INDEX IF NOT EXISTS idx_transactions_user_category
ON transactions(user_id, category);

-- ============================================
-- 3. ENABLE ROW LEVEL SECURITY (RLS)
-- ============================================

ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;

-- ============================================
-- 4. CREATE RLS POLICIES
-- ============================================

-- Policy: User hanya bisa melihat transaksi mereka sendiri
CREATE POLICY "Users can view own transactions"
ON transactions FOR SELECT
USING (auth.uid() = user_id);

-- Policy: User hanya bisa insert transaksi untuk diri mereka sendiri
CREATE POLICY "Users can insert own transactions"
ON transactions FOR INSERT
WITH CHECK (auth.uid() = user_id);

-- Policy: User hanya bisa update transaksi mereka sendiri
CREATE POLICY "Users can update own transactions"
ON transactions FOR UPDATE
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- Policy: User hanya bisa delete transaksi mereka sendiri
CREATE POLICY "Users can delete own transactions"
ON transactions FOR DELETE
USING (auth.uid() = user_id);

-- ============================================
-- 5. CREATE FUNCTIONS untuk CRUD
-- ============================================

-- Function untuk get transactions dengan pagination
CREATE OR REPLACE FUNCTION get_user_transactions(
    p_user_id uuid,
    p_limit int DEFAULT 50,
    p_offset int DEFAULT 0
)
RETURNS TABLE(
    id uuid,
    user_id uuid,
    title text,
    amount bigint,
    is_income boolean,
    category text,
    date timestamp with time zone,
    notes text,
    created_at timestamp with time zone,
    updated_at timestamp with time zone,
    total_count bigint
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        t.id,
        t.user_id,
        t.title,
        t.amount,
        t.is_income,
        t.category,
        t.date,
        t.notes,
        t.created_at,
        t.updated_at,
        COUNT(*) OVER () as total_count
    FROM transactions t
    WHERE t.user_id = p_user_id
    ORDER BY t.date DESC
    LIMIT p_limit OFFSET p_offset;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================
-- 6. CREATE VIEWS untuk Analytics
-- ============================================

-- View: Monthly Statistics
CREATE OR REPLACE VIEW user_monthly_stats AS
SELECT
    user_id,
    DATE_TRUNC('month', date)::DATE as month,
    SUM(CASE WHEN is_income THEN amount ELSE 0 END) as total_income,
    SUM(CASE WHEN NOT is_income THEN amount ELSE 0 END) as total_expense,
    SUM(CASE WHEN is_income THEN amount ELSE -amount END) as balance
FROM transactions
GROUP BY user_id, DATE_TRUNC('month', date);

-- View: Category Statistics
CREATE OR REPLACE VIEW user_category_stats AS
SELECT
    user_id,
    category,
    COUNT(*) as transaction_count,
    SUM(amount) as total_amount,
    AVG(amount) as average_amount,
    MAX(amount) as max_amount,
    MIN(amount) as min_amount
FROM transactions
GROUP BY user_id, category
ORDER BY user_id, total_amount DESC;

-- View: Daily Summary
CREATE OR REPLACE VIEW user_daily_summary AS
SELECT
    user_id,
    DATE(date) as transaction_date,
    SUM(CASE WHEN is_income THEN amount ELSE 0 END) as daily_income,
    SUM(CASE WHEN NOT is_income THEN amount ELSE 0 END) as daily_expense,
    SUM(CASE WHEN is_income THEN amount ELSE -amount END) as daily_balance,
    COUNT(*) as transaction_count
FROM transactions
GROUP BY user_id, DATE(date)
ORDER BY user_id, transaction_date DESC;

-- ============================================
-- 7. SAMPLE DATA (for testing)
-- ============================================

-- Note: Ganti 'YOUR_USER_ID' dengan UUID user yang sebenarnya dari auth.users

-- INSERT INTO transactions (user_id, title, amount, is_income, category, date, notes)
-- VALUES
--   ('YOUR_USER_ID', 'Gaji Bulanan', 5000000, true, 'Salary', NOW(), 'Gaji Desember 2024'),
--   ('YOUR_USER_ID', 'Makan Siang', 45000, false, 'Food', NOW(), 'Restoran'),
--   ('YOUR_USER_ID', 'Top-up OVO', 100000, false, 'Payment', NOW(), NULL),
--   ('YOUR_USER_ID', 'Freelance Project', 2000000, true, 'Freelance', NOW(), 'Membuat website'),
--   ('YOUR_USER_ID', 'Belanja Groceries', 250000, false, 'Shopping', NOW(), 'Supermarket');

-- ============================================
-- 8. USEFUL QUERIES
-- ============================================

-- Query: Get all transactions for a user ordered by date
-- SELECT * FROM transactions
-- WHERE user_id = 'YOUR_USER_ID'
-- ORDER BY date DESC;

-- Query: Get total income dan expense
-- SELECT
--   SUM(CASE WHEN is_income THEN amount ELSE 0 END) as total_income,
--   SUM(CASE WHEN NOT is_income THEN amount ELSE 0 END) as total_expense
-- FROM transactions
-- WHERE user_id = 'YOUR_USER_ID';

-- Query: Get transactions by category
-- SELECT * FROM transactions
-- WHERE user_id = 'YOUR_USER_ID' AND category = 'Food'
-- ORDER BY date DESC;

-- Query: Get transactions in date range
-- SELECT * FROM transactions
-- WHERE user_id = 'YOUR_USER_ID'
-- AND date BETWEEN '2024-12-01' AND '2024-12-31'
-- ORDER BY date DESC;

-- Query: Get summary by category
-- SELECT category,
--   COUNT(*) as count,
--   SUM(amount) as total,
--   AVG(amount) as average
-- FROM transactions
-- WHERE user_id = 'YOUR_USER_ID'
-- GROUP BY category
-- ORDER BY total DESC;

-- ============================================
-- 9. DROPS (jika perlu reset)
-- ============================================

-- DROP VIEW IF EXISTS user_daily_summary;
-- DROP VIEW IF EXISTS user_category_stats;
-- DROP VIEW IF EXISTS user_monthly_stats;
-- DROP FUNCTION IF EXISTS get_user_transactions(uuid, int, int);
-- DROP TABLE IF EXISTS transactions;

-- ============================================
-- TESTING CHECKLIST
-- ============================================

-- ✅ Run script di Supabase SQL Editor
-- ✅ Verify table transactions dibuat
-- ✅ Verify indexes dibuat
-- ✅ Verify RLS policies aktif
-- ✅ Insert sample data
-- ✅ Query SELECT untuk verify data
-- ✅ Test RLS policy (SELECT hanya data milik user sendiri)
-- ✅ Check views dapat diakses
-- ✅ Verify performance dengan EXPLAIN

-- ============================================
-- NOTES
-- ============================================

-- 1. Ganti 'YOUR_USER_ID' dengan UUID dari auth.users
-- 2. Pastikan auth.users table sudah ada (auto-created dengan Supabase)
-- 3. RLS policy harus enabled agar security ketat
-- 4. Indexes membantu performa query, terutama untuk table besar
-- 5. Views mempermudah query analytics tanpa logic kompleks di app

-- ============================================

