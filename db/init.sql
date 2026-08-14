-- Entitlements POC schema
-- Mirrors the shape of a SailPoint-style entitlement catalog:
-- applications (target systems) -> entitlements (cryptic access items)
-- users -> user_entitlements (grants)
-- entitlement_descriptions (populated OFFLINE by the llm-utility, never at request time)

CREATE TABLE applications (
    application_id      SERIAL PRIMARY KEY,
    name                 VARCHAR(100) NOT NULL,
    source_system        VARCHAR(100) NOT NULL,   -- e.g. SailPoint connector type
    description           TEXT
);

CREATE TABLE entitlements (
    entitlement_id       SERIAL PRIMARY KEY,
    application_id        INTEGER NOT NULL REFERENCES applications(application_id),
    cryptic_title          VARCHAR(150) NOT NULL,   -- the raw, unreadable code as it comes from the source system
    entitlement_type      VARCHAR(50),              -- role, group, permission, profile...
    raw_attributes         JSONB,                    -- whatever metadata the connector happened to bring over
    UNIQUE (application_id, cryptic_title)
);

CREATE TABLE users (
    user_id                SERIAL PRIMARY KEY,
    employee_id             VARCHAR(20) UNIQUE NOT NULL,
    full_name               VARCHAR(150) NOT NULL,
    department              VARCHAR(100),
    title                    VARCHAR(100),
    manager_name            VARCHAR(150)
);

CREATE TABLE user_entitlements (
    user_id                 INTEGER NOT NULL REFERENCES users(user_id),
    entitlement_id           INTEGER NOT NULL REFERENCES entitlements(entitlement_id),
    granted_date              DATE DEFAULT CURRENT_DATE,
    PRIMARY KEY (user_id, entitlement_id)
);

-- Populated OFFLINE by the LLM utility. One row per entitlement (1:1), never generated on the fly.
CREATE TABLE entitlement_descriptions (
    entitlement_id            INTEGER PRIMARY KEY REFERENCES entitlements(entitlement_id),
    description                 TEXT NOT NULL,
    risk_note                    TEXT,               -- optional: e.g. "Privileged - grants production write access"
    generated_by_model           VARCHAR(100),
    generated_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------------
-- Seed data
-- ---------------------------------------------------------------------------

INSERT INTO applications (name, source_system, description) VALUES
('SAP ERP Financials', 'SAP GRC Connector', 'General ledger, accounts payable/receivable, financial approvals'),
('Mainframe CICS', 'RACF Connector', 'Core banking transaction processing on the mainframe'),
('Active Directory', 'AD Connector', 'Corporate network, file shares, and Windows resource access'),
('Salesforce', 'SFDC Connector', 'Client relationship management for Wealth Management'),
('Unix/Linux Servers', 'Unix Connector', 'Production and non-production server access');

INSERT INTO entitlements (application_id, cryptic_title, entitlement_type, raw_attributes) VALUES
(1, 'SAP_FI_GL_APRV_L3',          'role',       '{"module":"FI","function":"GL Approval","level":3}'),
(1, 'SAP_FI_AP_CREATE_INV',       'role',       '{"module":"FI","function":"AP Invoice Create"}'),
(1, 'SAP_FI_AP_APPROVE_INV_L2',   'role',       '{"module":"FI","function":"AP Invoice Approval","level":2}'),
(1, 'SAP_CO_COST_CTR_MAINT',      'role',       '{"module":"CO","function":"Cost Center Maintenance"}'),
(1, 'SAP_FI_AUDIT_READONLY',      'role',       '{"module":"FI","function":"Audit Read Only"}'),
(1, 'SAP_TREASURY_WIRE_INIT',     'role',       '{"module":"TR","function":"Wire Initiation"}'),
(1, 'SAP_TREASURY_WIRE_APRV',     'role',       '{"module":"TR","function":"Wire Approval"}'),

(2, 'MF_CICS_TXN_UPD_DDA',        'transaction','{"region":"PROD","txn_group":"DDA Update"}'),
(2, 'MF_CICS_TXN_VIEW_DDA',       'transaction','{"region":"PROD","txn_group":"DDA View"}'),
(2, 'MF_CICS_TXN_UPD_LOAN',       'transaction','{"region":"PROD","txn_group":"Loan Update"}'),
(2, 'MF_RACF_SYSPROG',            'group',      '{"privilege":"system programmer"}'),
(2, 'MF_CICS_OVERRIDE_LIMIT',     'transaction','{"txn_group":"Override Daily Limit"}'),

(3, 'AD_GRP_FinOps_RW',           'group',      '{"share":"finops-shared","access":"read-write"}'),
(3, 'AD_GRP_VPN_Contractors',     'group',      '{"resource":"VPN","population":"contractors"}'),
(3, 'AD_GRP_DomainAdmins',        'group',      '{"privilege":"domain admin"}'),
(3, 'AD_GRP_PrintAdmins',         'group',      '{"privilege":"print server admin"}'),
(3, 'AD_GRP_HR_Confidential_RO',  'group',      '{"share":"hr-confidential","access":"read-only"}'),

(4, 'SFDC_PROFILE_WM_Advisor',    'profile',    '{"business_unit":"Wealth Management","role":"Advisor"}'),
(4, 'SFDC_PROFILE_WM_OpsAdmin',   'profile',    '{"business_unit":"Wealth Management","role":"Ops Admin"}'),
(4, 'SFDC_PERM_ExportClientData', 'permission', '{"function":"bulk export"}'),
(4, 'SFDC_PERM_ModifyAllData',    'permission', '{"function":"modify all data - org wide"}'),

(5, 'UNIX_SUDO_DBA_PROD',         'permission', '{"env":"production","role":"database admin"}'),
(5, 'UNIX_GRP_appdeploy',         'group',      '{"env":"production","function":"application deployment"}'),
(5, 'UNIX_SUDO_NETADMIN',         'permission', '{"env":"production","role":"network admin"}'),
(5, 'UNIX_GRP_readonly_batch',    'group',      '{"env":"production","function":"batch job read-only"}');

INSERT INTO users (employee_id, full_name, department, title, manager_name) VALUES
('E10234', 'Priya Natarajan',  'Finance Operations',      'Senior Financial Analyst',    'Robert Kim'),
('E10298', 'Marcus Webb',      'Core Banking Systems',    'Mainframe Systems Engineer',  'Linda Ostrowski'),
('E10355', 'Aisha Bello',      'Wealth Management',       'Client Advisor',              'Devon Marsh'),
('E10402', 'Thomas Reilly',    'IT Infrastructure',       'Unix Systems Administrator',  'Linda Ostrowski'),
('E10467', 'Sun-Hee Park',     'Internal Audit',          'IT Auditor',                  'Robert Kim'),
('E10521', 'Diego Alvarez',    'Treasury',                'Treasury Analyst',            'Robert Kim');

-- Grants: mix of routine and a couple of "should probably be reviewed" combinations,
-- which is exactly the kind of thing an access reviewer needs plain-English help with.
INSERT INTO user_entitlements (user_id, entitlement_id) VALUES
(1, 1), (1, 2), (1, 4),                          -- Priya: GL approval, AP create, cost center
(2, 8), (2, 9), (2, 10), (2, 11),                -- Marcus: mainframe txn access + sysprog
(3, 18), (3, 20),                                -- Aisha: WM advisor profile + export client data
(4, 22), (4, 23), (4, 24),                       -- Thomas: sudo dba, deploy group, sudo netadmin
(5, 5), (5, 21),                                 -- Sun-Hee: audit read-only + SFDC modify-all (flagged combo!)
(6, 6), (6, 7);                                  -- Diego: wire initiation AND wire approval (segregation-of-duties flag!)
