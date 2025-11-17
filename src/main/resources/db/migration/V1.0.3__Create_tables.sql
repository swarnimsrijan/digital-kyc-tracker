--CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT chk_username_length CHECK (LENGTH(username) BETWEEN 5 AND 50),
    CONSTRAINT chk_password_length CHECK (LENGTH(password) >= 8)
);

-- Verification requests table
CREATE TABLE IF NOT EXISTS verification_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    requestor_id UUID NOT NULL,
    assigned_officer_id UUID,
    status VARCHAR(50) NOT NULL,
    request_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    approved_at TIMESTAMP,
    rejected_at TIMESTAMP,
    CONSTRAINT fk_verification_customer
        FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_verification_requestor
        FOREIGN KEY (requestor_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_verification_officer
        FOREIGN KEY (assigned_officer_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Documents table
CREATE TABLE IF NOT EXISTS documents (
    document_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    verification_request_id UUID NOT NULL,
    uploaded_by UUID NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size NUMERIC(15,2) NOT NULL,
    file_data BYTEA,
    content_type VARCHAR(100) NOT NULL,
    file_hash VARCHAR(64) UNIQUE NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(500),
    CONSTRAINT fk_document_verification_request
        FOREIGN KEY (verification_request_id)
        REFERENCES verification_requests(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_document_uploaded_by
        FOREIGN KEY (uploaded_by)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_file_name_length CHECK (LENGTH(file_name) > 0),
    CONSTRAINT chk_file_size_positive CHECK (file_size > 0)
);


-- Comments table
CREATE TABLE IF NOT EXISTS comments (
    comment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    verification_request_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    comment_text TEXT NOT NULL,
    comment_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_comment_verification_request
        FOREIGN KEY (verification_request_id)
        REFERENCES verification_requests(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_comment_sender
        FOREIGN KEY (sender_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- Status history table
CREATE TABLE IF NOT EXISTS status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    verification_request_id UUID NOT NULL,
    changed_by UUID NOT NULL,
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    reason TEXT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_status_history_verification_request
        FOREIGN KEY (verification_request_id)
        REFERENCES verification_requests(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_status_history_changed_by
        FOREIGN KEY (changed_by)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    verification_request_id UUID NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_notification_verification FOREIGN KEY (verification_request_id) REFERENCES verification_requests(id)
);


-- Audit logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    audit_log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    audit_action VARCHAR(50) NOT NULL,
    user_id UUID,
    old_value TEXT,
    new_value TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Verification request limits table
CREATE TABLE IF NOT EXISTS verification_request_limits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    verification_requestor UUID NOT NULL,
    year INTEGER NOT NULL,
    request_count_by_requestor INTEGER DEFAULT 0,
    total_requests INTEGER DEFAULT 0,
    max_allowed_requests INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_limit_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_limit_requestor FOREIGN KEY (verification_requestor) REFERENCES users(id),
    CONSTRAINT uq_customer_requestor_year UNIQUE (customer_id, verification_requestor, year),
    CONSTRAINT chk_year_valid CHECK (year >= 2000 AND year <= 2100),
    CONSTRAINT chk_request_count_positive CHECK (request_count_by_requestor >= 0),
    CONSTRAINT chk_total_requests_positive CHECK (total_requests >= 0),
    CONSTRAINT chk_max_allowed_positive CHECK (max_allowed_requests > 0)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);

CREATE INDEX idx_verification_customer_status ON verification_requests(customer_id, status);
CREATE INDEX idx_verification_requestor_status ON verification_requests(requestor_id, status);
CREATE INDEX idx_verification_officer_status ON verification_requests(assigned_officer_id, status) WHERE assigned_officer_id IS NOT NULL;

CREATE INDEX idx_verification_status ON verification_requests(status);
CREATE INDEX idx_verification_created ON verification_requests(created_at DESC);

CREATE INDEX idx_documents_verification_active ON documents(verification_request_id, is_active);

CREATE INDEX idx_documents_uploader ON documents(uploaded_by);
CREATE INDEX idx_documents_type ON documents(document_type);
CREATE INDEX idx_documents_uploaded ON documents(uploaded_at DESC);

CREATE INDEX idx_documents_active_only ON documents(verification_request_id) WHERE is_active = TRUE;

CREATE INDEX idx_comments_verification ON comments(verification_request_id, created_at DESC);
CREATE INDEX idx_comments_type ON comments(comment_type);

CREATE INDEX idx_status_verification_time ON status_history(verification_request_id, changed_at DESC);
CREATE INDEX idx_status_changed_by ON status_history(changed_by);
CREATE INDEX idx_status_to_status ON status_history(to_status);

CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read, created_at DESC);

CREATE INDEX idx_notifications_unread_only ON notifications(user_id, created_at DESC) WHERE is_read = FALSE;

CREATE INDEX idx_notifications_verification ON notifications(verification_request_id);
CREATE INDEX idx_notifications_type ON notifications(notification_type);