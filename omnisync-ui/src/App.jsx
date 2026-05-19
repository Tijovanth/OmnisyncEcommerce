import { useState, useEffect, useCallback } from "react";

/* ─────────────────────────────────────────────
   CONSTANTS
───────────────────────────────────────────── */
const API_BASE = "http://localhost:8080";

const apiFetch = async (path, options = {}) => {
  const res = await fetch(`${API_BASE}${path}`, {
    credentials: "include",
    redirect: "manual",
    headers: { "Content-Type": "application/json", ...options.headers },
    ...options,
  });
  if (res.status === 401 || res.status === 302 || res.type === "opaqueredirect") {
    window.location.href = `${API_BASE}/oauth2/authorization/keycloak`;
    return null;
  }
  return res;
};

/* ─────────────────────────────────────────────
   STYLES
───────────────────────────────────────────── */
const styles = `
  @import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&family=DM+Sans:ital,opsz,wght@0,9..40,300;0,9..40,400;0,9..40,500;1,9..40,300&display=swap');

  *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

  :root {
    --bg: #0a0a0f;
    --bg2: #111118;
    --bg3: #1a1a25;
    --border: rgba(255,255,255,0.07);
    --accent: #6c63ff;
    --accent2: #ff6b9d;
    --accent3: #00d4aa;
    --text: #f0f0f8;
    --text-muted: #7a7a99;
    --card: #13131e;
    --card-hover: #1e1e2e;
    --danger: #ff4757;
    --success: #2ed573;
    --warning: #ffa502;
    --radius: 14px;
    --radius-sm: 8px;
    --shadow: 0 8px 32px rgba(0,0,0,0.5);
    --glow: 0 0 40px rgba(108,99,255,0.15);
  }

  body {
    background: var(--bg);
    color: var(--text);
    font-family: 'DM Sans', sans-serif;
    min-height: 100vh;
    line-height: 1.6;
  }

  /* ── AUTH PAGE ── */
  .auth-page {
    min-height: 100vh;
    display: grid;
    place-items: center;
    background: radial-gradient(ellipse 80% 60% at 50% 0%, rgba(108,99,255,0.18) 0%, transparent 70%),
                radial-gradient(ellipse 50% 40% at 80% 80%, rgba(255,107,157,0.10) 0%, transparent 60%),
                var(--bg);
    position: relative;
    overflow: hidden;
  }
  .auth-page::before {
    content: '';
    position: absolute;
    inset: 0;
    background-image: 
      radial-gradient(circle at 1px 1px, rgba(255,255,255,0.03) 1px, transparent 0);
    background-size: 40px 40px;
    pointer-events: none;
  }
  .auth-card {
    background: var(--card);
    border: 1px solid var(--border);
    border-radius: 24px;
    padding: 56px 48px;
    width: min(460px, 92vw);
    box-shadow: var(--shadow), var(--glow);
    position: relative;
    z-index: 1;
    text-align: center;
  }
  .auth-logo {
    font-family: 'Syne', sans-serif;
    font-size: 2rem;
    font-weight: 800;
    letter-spacing: -1px;
    background: linear-gradient(135deg, var(--accent), var(--accent2));
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin-bottom: 6px;
  }
  .auth-tagline {
    color: var(--text-muted);
    font-size: 0.9rem;
    margin-bottom: 40px;
  }
  .auth-btn {
    width: 100%;
    padding: 16px;
    background: linear-gradient(135deg, var(--accent), #8b85ff);
    border: none;
    border-radius: var(--radius);
    color: white;
    font-family: 'Syne', sans-serif;
    font-weight: 700;
    font-size: 1rem;
    letter-spacing: 0.5px;
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s, opacity 0.2s;
    box-shadow: 0 4px 20px rgba(108,99,255,0.4);
  }
  .auth-btn:hover { transform: translateY(-2px); box-shadow: 0 8px 28px rgba(108,99,255,0.5); }
  .auth-btn:active { transform: translateY(0); }
  .auth-features {
    margin-top: 36px;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  .auth-feature {
    display: flex;
    align-items: center;
    gap: 10px;
    color: var(--text-muted);
    font-size: 0.85rem;
  }
  .auth-feature-dot {
    width: 6px; height: 6px;
    border-radius: 50%;
    background: var(--accent3);
    flex-shrink: 0;
  }

  /* ── LAYOUT ── */
  .app-shell {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
  }
  .navbar {
    position: sticky;
    top: 0;
    z-index: 100;
    background: rgba(10,10,15,0.85);
    backdrop-filter: blur(20px);
    border-bottom: 1px solid var(--border);
    padding: 0 32px;
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  .nav-brand {
    font-family: 'Syne', sans-serif;
    font-weight: 800;
    font-size: 1.25rem;
    letter-spacing: -0.5px;
    background: linear-gradient(135deg, var(--accent), var(--accent2));
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  .nav-right { display: flex; align-items: center; gap: 16px; }
  .role-badge {
    padding: 4px 12px;
    border-radius: 999px;
    font-size: 0.75rem;
    font-weight: 600;
    letter-spacing: 0.5px;
    text-transform: uppercase;
  }
  .role-badge.admin { background: rgba(108,99,255,0.15); color: var(--accent); border: 1px solid rgba(108,99,255,0.3); }
  .role-badge.customer { background: rgba(0,212,170,0.12); color: var(--accent3); border: 1px solid rgba(0,212,170,0.25); }
  .user-name { color: var(--text-muted); font-size: 0.875rem; }
  .logout-btn {
    padding: 8px 18px;
    background: transparent;
    border: 1px solid var(--border);
    border-radius: var(--radius-sm);
    color: var(--text-muted);
    font-family: 'DM Sans', sans-serif;
    font-size: 0.875rem;
    cursor: pointer;
    transition: all 0.2s;
  }
  .logout-btn:hover { border-color: var(--danger); color: var(--danger); background: rgba(255,71,87,0.06); }

  .main-content {
    flex: 1;
    padding: 40px 32px;
    max-width: 1200px;
    margin: 0 auto;
    width: 100%;
  }

  /* ── SECTION HEADER ── */
  .page-header {
    margin-bottom: 36px;
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 16px;
    flex-wrap: wrap;
  }
  .page-title {
    font-family: 'Syne', sans-serif;
    font-size: 2rem;
    font-weight: 800;
    letter-spacing: -1px;
    color: var(--text);
  }
  .page-sub { color: var(--text-muted); font-size: 0.9rem; margin-top: 4px; }

  /* ── CARDS ── */
  .card {
    background: var(--card);
    border: 1px solid var(--border);
    border-radius: var(--radius);
    padding: 28px;
    transition: border-color 0.2s, box-shadow 0.2s;
  }
  .card:hover { border-color: rgba(108,99,255,0.2); box-shadow: var(--glow); }
  .card-title {
    font-family: 'Syne', sans-serif;
    font-weight: 700;
    font-size: 1.1rem;
    margin-bottom: 20px;
    color: var(--text);
  }

  /* ── GRID ── */
  .grid-2 { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 20px; }
  .grid-3 { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }

  /* ── PRODUCT CARD ── */
  .product-card {
    background: var(--card);
    border: 1px solid var(--border);
    border-radius: var(--radius);
    padding: 22px;
    cursor: pointer;
    transition: all 0.2s;
    position: relative;
    overflow: hidden;
  }
  .product-card::before {
    content: '';
    position: absolute;
    top: 0; left: 0;
    right: 0; height: 2px;
    background: linear-gradient(90deg, var(--accent), var(--accent2));
    opacity: 0;
    transition: opacity 0.2s;
  }
  .product-card:hover { transform: translateY(-3px); box-shadow: var(--shadow); border-color: rgba(108,99,255,0.25); }
  .product-card:hover::before { opacity: 1; }
  .product-card.selected { border-color: var(--accent); box-shadow: 0 0 0 2px rgba(108,99,255,0.25); }
  .product-card.selected::before { opacity: 1; }
  .product-name { font-family: 'Syne', sans-serif; font-weight: 700; font-size: 1rem; margin-bottom: 8px; }
  .product-price { font-size: 1.4rem; font-weight: 700; color: var(--accent); font-family: 'Syne', sans-serif; }
  .product-meta { display: flex; gap: 12px; margin-top: 10px; flex-wrap: wrap; }
  .product-tag {
    padding: 3px 10px;
    border-radius: 999px;
    font-size: 0.75rem;
    font-weight: 500;
  }
  .tag-stock { background: rgba(0,212,170,0.1); color: var(--accent3); }
  .tag-out { background: rgba(255,71,87,0.1); color: var(--danger); }
  .tag-cat { background: rgba(108,99,255,0.1); color: var(--accent); }

  /* ── FORM ── */
  .form-group { margin-bottom: 20px; }
  .form-label {
    display: block;
    font-size: 0.8rem;
    font-weight: 600;
    letter-spacing: 0.5px;
    text-transform: uppercase;
    color: var(--text-muted);
    margin-bottom: 8px;
  }
  .form-input, .form-select {
    width: 100%;
    padding: 12px 16px;
    background: var(--bg2);
    border: 1px solid var(--border);
    border-radius: var(--radius-sm);
    color: var(--text);
    font-family: 'DM Sans', sans-serif;
    font-size: 0.95rem;
    transition: border-color 0.2s, box-shadow 0.2s;
    outline: none;
    appearance: none;
  }
  .form-input:focus, .form-select:focus {
    border-color: var(--accent);
    box-shadow: 0 0 0 3px rgba(108,99,255,0.12);
  }
  .form-input::placeholder { color: var(--text-muted); }
  .form-select option { background: var(--bg2); color: var(--text); }

  /* ── BUTTONS ── */
  .btn {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 12px 22px;
    border-radius: var(--radius-sm);
    font-family: 'Syne', sans-serif;
    font-weight: 600;
    font-size: 0.9rem;
    cursor: pointer;
    border: none;
    transition: all 0.2s;
    text-decoration: none;
    white-space: nowrap;
  }
  .btn:disabled { opacity: 0.45; cursor: not-allowed; }
  .btn-primary {
    background: linear-gradient(135deg, var(--accent), #8b85ff);
    color: white;
    box-shadow: 0 4px 16px rgba(108,99,255,0.35);
  }
  .btn-primary:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(108,99,255,0.45); }
  .btn-secondary {
    background: var(--bg3);
    color: var(--text);
    border: 1px solid var(--border);
  }
  .btn-secondary:hover:not(:disabled) { border-color: rgba(255,255,255,0.15); background: var(--card-hover); }
  .btn-success {
    background: linear-gradient(135deg, var(--accent3), #00b894);
    color: #002e25;
    box-shadow: 0 4px 16px rgba(0,212,170,0.25);
    font-weight: 700;
  }
  .btn-success:hover:not(:disabled) { transform: translateY(-1px); }
  .btn-danger {
    background: rgba(255,71,87,0.12);
    color: var(--danger);
    border: 1px solid rgba(255,71,87,0.25);
  }
  .btn-danger:hover:not(:disabled) { background: rgba(255,71,87,0.2); }
  .btn-sm { padding: 7px 14px; font-size: 0.8rem; }
  .btn-icon { padding: 8px; border-radius: var(--radius-sm); }

  /* ── TOAST ── */
  .toast-container {
    position: fixed;
    bottom: 24px;
    right: 24px;
    z-index: 9999;
    display: flex;
    flex-direction: column;
    gap: 10px;
    pointer-events: none;
  }
  .toast {
    padding: 14px 20px;
    border-radius: var(--radius);
    font-size: 0.9rem;
    box-shadow: var(--shadow);
    pointer-events: all;
    animation: toastIn 0.3s ease;
    max-width: 360px;
    display: flex;
    align-items: center;
    gap: 10px;
    border-left: 3px solid;
  }
  .toast.success { background: var(--card); border-color: var(--success); }
  .toast.error { background: var(--card); border-color: var(--danger); }
  .toast.info { background: var(--card); border-color: var(--accent); }
  @keyframes toastIn { from { transform: translateX(120%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }

  /* ── MODAL ── */
  .modal-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0,0,0,0.7);
    backdrop-filter: blur(6px);
    z-index: 500;
    display: grid;
    place-items: center;
    padding: 20px;
    animation: fadeIn 0.2s ease;
  }
  @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
  .modal {
    background: var(--card);
    border: 1px solid var(--border);
    border-radius: 20px;
    padding: 36px;
    width: min(520px, 100%);
    box-shadow: var(--shadow), var(--glow);
    animation: slideUp 0.25s ease;
    max-height: 90vh;
    overflow-y: auto;
  }
  @keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 28px;
  }
  .modal-title {
    font-family: 'Syne', sans-serif;
    font-weight: 800;
    font-size: 1.3rem;
    letter-spacing: -0.5px;
  }
  .modal-close {
    background: none;
    border: none;
    color: var(--text-muted);
    font-size: 1.3rem;
    cursor: pointer;
    padding: 4px;
    border-radius: 6px;
    transition: color 0.2s;
    line-height: 1;
  }
  .modal-close:hover { color: var(--text); }

  /* ── DIVIDER ── */
  .divider { height: 1px; background: var(--border); margin: 24px 0; }

  /* ── STATS ── */
  .stat-bar { display: flex; gap: 16px; margin-bottom: 32px; flex-wrap: wrap; }
  .stat-item {
    background: var(--card);
    border: 1px solid var(--border);
    border-radius: var(--radius);
    padding: 20px 24px;
    flex: 1;
    min-width: 140px;
  }
  .stat-value { font-family: 'Syne', sans-serif; font-size: 2rem; font-weight: 800; letter-spacing: -1px; }
  .stat-label { color: var(--text-muted); font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.5px; margin-top: 4px; }

  /* ── ORDER ITEM ── */
  .order-item-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 14px;
    background: var(--bg2);
    border-radius: var(--radius-sm);
    border: 1px solid var(--border);
    margin-bottom: 10px;
  }
  .order-item-info { flex: 1; min-width: 0; }
  .order-item-name { font-weight: 600; font-size: 0.95rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  .order-item-price { color: var(--text-muted); font-size: 0.8rem; margin-top: 2px; }
  .qty-control { display: flex; align-items: center; gap: 8px; }
  .qty-btn {
    width: 28px; height: 28px;
    background: var(--bg3);
    border: 1px solid var(--border);
    border-radius: 6px;
    color: var(--text);
    cursor: pointer;
    display: grid;
    place-items: center;
    font-size: 1rem;
    font-weight: 700;
    transition: all 0.15s;
    line-height: 1;
  }
  .qty-btn:hover { border-color: var(--accent); color: var(--accent); }
  .qty-display { font-family: 'Syne', sans-serif; font-weight: 700; font-size: 0.95rem; min-width: 20px; text-align: center; }
  .order-item-subtotal { font-family: 'Syne', sans-serif; font-weight: 700; color: var(--accent); font-size: 0.95rem; min-width: 70px; text-align: right; }

  /* ── SUCCESS SCREEN ── */
  .success-screen {
    text-align: center;
    padding: 32px 0 8px;
  }
  .success-icon {
    width: 72px; height: 72px;
    border-radius: 50%;
    background: rgba(46,213,115,0.12);
    border: 2px solid var(--success);
    display: grid;
    place-items: center;
    margin: 0 auto 20px;
    font-size: 2rem;
  }
  .success-title {
    font-family: 'Syne', sans-serif;
    font-size: 1.5rem;
    font-weight: 800;
    letter-spacing: -0.5px;
    margin-bottom: 8px;
  }
  .order-number {
    display: inline-block;
    padding: 8px 18px;
    background: rgba(108,99,255,0.1);
    border: 1px solid rgba(108,99,255,0.25);
    border-radius: 999px;
    font-family: 'Syne', sans-serif;
    font-weight: 700;
    color: var(--accent);
    margin-top: 12px;
    font-size: 0.9rem;
    word-break: break-all;
  }

  /* ── CATEGORY SECTION ── */
  .category-pill {
    padding: 8px 20px;
    border-radius: 999px;
    border: 1px solid var(--border);
    background: var(--card);
    color: var(--text-muted);
    font-size: 0.875rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    white-space: nowrap;
  }
  .category-pill:hover { border-color: var(--accent); color: var(--accent); background: rgba(108,99,255,0.06); }
  .category-pill.active { border-color: var(--accent); color: white; background: linear-gradient(135deg, var(--accent), #8b85ff); box-shadow: 0 4px 16px rgba(108,99,255,0.3); }
  .category-pills { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 28px; }

  /* ── LOADING ── */
  .spinner {
    width: 20px; height: 20px;
    border: 2px solid rgba(255,255,255,0.2);
    border-top-color: white;
    border-radius: 50%;
    animation: spin 0.7s linear infinite;
    display: inline-block;
  }
  @keyframes spin { to { transform: rotate(360deg); } }
  .loading-center { display: flex; justify-content: center; align-items: center; padding: 60px; }

  /* ── EMPTY STATE ── */
  .empty-state {
    text-align: center;
    padding: 60px 20px;
    color: var(--text-muted);
  }
  .empty-icon { font-size: 3rem; margin-bottom: 16px; opacity: 0.5; }
  .empty-title { font-family: 'Syne', sans-serif; font-size: 1.1rem; font-weight: 700; margin-bottom: 8px; }

  /* ── SCROLL ── */
  ::-webkit-scrollbar { width: 6px; height: 6px; }
  ::-webkit-scrollbar-track { background: transparent; }
  ::-webkit-scrollbar-thumb { background: var(--border); border-radius: 3px; }
  ::-webkit-scrollbar-thumb:hover { background: rgba(255,255,255,0.15); }

  /* ── TOTAL BAR ── */
  .total-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background: rgba(108,99,255,0.08);
    border: 1px solid rgba(108,99,255,0.2);
    border-radius: var(--radius-sm);
    margin: 16px 0;
  }
  .total-label { font-size: 0.85rem; color: var(--text-muted); font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; }
  .total-value { font-family: 'Syne', sans-serif; font-size: 1.5rem; font-weight: 800; color: var(--accent); }

  /* ── TABS ── */
  .tabs { display: flex; gap: 4px; background: var(--bg2); padding: 4px; border-radius: 10px; margin-bottom: 28px; width: fit-content; }
  .tab-btn {
    padding: 9px 20px;
    border: none;
    border-radius: 7px;
    background: transparent;
    color: var(--text-muted);
    font-family: 'Syne', sans-serif;
    font-weight: 600;
    font-size: 0.875rem;
    cursor: pointer;
    transition: all 0.2s;
  }
  .tab-btn.active { background: var(--card); color: var(--text); box-shadow: 0 2px 8px rgba(0,0,0,0.3); }

  /* ── CAT LIST ── */
  .cat-list-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 18px;
    background: var(--bg2);
    border: 1px solid var(--border);
    border-radius: var(--radius-sm);
    margin-bottom: 10px;
  }
  .cat-list-name { font-weight: 600; font-size: 0.95rem; }
  .cat-list-id { color: var(--text-muted); font-size: 0.8rem; font-family: monospace; }

  /* ── WELCOME ── */
  .welcome-bar {
    background: linear-gradient(135deg, rgba(108,99,255,0.12), rgba(255,107,157,0.08));
    border: 1px solid rgba(108,99,255,0.2);
    border-radius: var(--radius);
    padding: 24px 28px;
    margin-bottom: 32px;
  }
  .welcome-greeting { color: var(--text-muted); font-size: 0.875rem; margin-bottom: 4px; }
  .welcome-name { font-family: 'Syne', sans-serif; font-size: 1.4rem; font-weight: 800; }

  @media (max-width: 640px) {
    .main-content { padding: 24px 16px; }
    .navbar { padding: 0 16px; }
    .auth-card { padding: 36px 24px; }
    .modal { padding: 24px; }
    .page-title { font-size: 1.5rem; }
  }
`;

/* ─────────────────────────────────────────────
   HOOKS
───────────────────────────────────────────── */
function useToast() {
  const [toasts, setToasts] = useState([]);
  const add = useCallback((msg, type = "info") => {
    const id = Date.now();
    setToasts((t) => [...t, { id, msg, type }]);
    setTimeout(() => setToasts((t) => t.filter((x) => x.id !== id)), 4000);
  }, []);
  return { toasts, toast: add };
}

/* ─────────────────────────────────────────────
   SMALL COMPONENTS
───────────────────────────────────────────── */
function ToastContainer({ toasts }) {
  const icons = { success: "✓", error: "✕", info: "ℹ" };
  return (
    <div className="toast-container">
      {toasts.map((t) => (
        <div key={t.id} className={`toast ${t.type}`}>
          <span>{icons[t.type]}</span> {t.msg}
        </div>
      ))}
    </div>
  );
}

function Modal({ title, onClose, children }) {
  return (
    <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <div className="modal-header">
          <h2 className="modal-title">{title}</h2>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>
        {children}
      </div>
    </div>
  );
}

function Spinner() {
  return <span className="spinner" />;
}

/* ─────────────────────────────────────────────
   AUTH PAGE
───────────────────────────────────────────── */
function AuthPage() {
  const handleLogin = () => {
    window.location.href = `${API_BASE}/oauth2/authorization/keycloak`;
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-logo">OmniSync</div>
        <p className="auth-tagline">Enterprise E-Commerce Platform</p>

        <button className="auth-btn" onClick={handleLogin}>
          Sign in with Keycloak
        </button>

        <div className="auth-features">
          {[
            "OAuth2 Authorization Code Flow with OIDC",
            "Role-based access control (Admin / Customer)",
            "Secure session via JSessionId cookie",
            "Connected to Spring Boot microservices",
          ].map((f) => (
            <div className="auth-feature" key={f}>
              <div className="auth-feature-dot" />
              <span>{f}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

/* ─────────────────────────────────────────────
   ADMIN – CREATE CATEGORY MODAL
───────────────────────────────────────────── */
function CreateCategoryModal({ onClose, onSuccess, toast }) {
  const [name, setName] = useState("");
  const [loading, setLoading] = useState(false);

  const submit = async () => {
    if (!name.trim()) return toast("Category name is required", "error");
    setLoading(true);
    try {
      const res = await apiFetch("/api/v1/categories", {
        method: "POST",
        body: JSON.stringify({ name: name.trim() }),
      });
      if (!res) return;
      if (res.ok) {
        const data = await res.json();
        toast(`Category "${data.name}" created!`, "success");
        onSuccess(data);
        onClose();
      } else {
        const err = await res.json().catch(() => ({}));
        toast(err.message || "Failed to create category", "error");
      }
    } catch {
      toast("Network error", "error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal title="Create Category" onClose={onClose}>
      <div className="form-group">
        <label className="form-label">Category Name</label>
        <input
          className="form-input"
          placeholder="e.g. Electronics, Clothing..."
          value={name}
          onChange={(e) => setName(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && submit()}
          autoFocus
        />
      </div>
      <div style={{ display: "flex", gap: 12, justifyContent: "flex-end" }}>
        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
        <button className="btn btn-primary" onClick={submit} disabled={loading}>
          {loading ? <Spinner /> : "Create Category"}
        </button>
      </div>
    </Modal>
  );
}

/* ─────────────────────────────────────────────
   ADMIN – CREATE PRODUCT MODAL
───────────────────────────────────────────── */
function CreateProductModal({ categories, onClose, onSuccess, toast }) {
  const [form, setForm] = useState({ name: "", stockQuantity: "", price: "", categoryId: "" });
  const [loading, setLoading] = useState(false);

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  const submit = async () => {
    if (!form.name.trim()) return toast("Product name is required", "error");
    if (!form.price) return toast("Price is required", "error");
    if (!form.categoryId) return toast("Please select a category", "error");

    setLoading(true);
    try {
      const res = await apiFetch("/api/v1/products", {
        method: "POST",
        body: JSON.stringify({
          name: form.name.trim(),
          stockQuantity: form.stockQuantity ? parseInt(form.stockQuantity) : 0,
          price: parseFloat(form.price),
          categoryId: parseInt(form.categoryId),
        }),
      });
      if (!res) return;
      if (res.ok) {
        const data = await res.json();
        toast(`Product "${data.name}" created!`, "success");
        onSuccess(data);
        onClose();
      } else {
        const err = await res.json().catch(() => ({}));
        toast(err.message || "Failed to create product", "error");
      }
    } catch {
      toast("Network error", "error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal title="Create Product" onClose={onClose}>
      <div className="form-group">
        <label className="form-label">Product Name</label>
        <input className="form-input" placeholder="e.g. iPhone 15 Pro" value={form.name} onChange={set("name")} autoFocus />
      </div>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
        <div className="form-group">
          <label className="form-label">Price (₹)</label>
          <input className="form-input" type="number" min="0" step="0.01" placeholder="0.00" value={form.price} onChange={set("price")} />
        </div>
        <div className="form-group">
          <label className="form-label">Stock Quantity</label>
          <input className="form-input" type="number" min="0" placeholder="0" value={form.stockQuantity} onChange={set("stockQuantity")} />
        </div>
      </div>
      <div className="form-group">
        <label className="form-label">Category</label>
        <select className="form-select" value={form.categoryId} onChange={set("categoryId")}>
          <option value="">-- Select category --</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
        {categories.length === 0 && (
          <p style={{ color: "var(--warning)", fontSize: "0.8rem", marginTop: 6 }}>
            ⚠ No categories yet — create one first.
          </p>
        )}
      </div>
      <div style={{ display: "flex", gap: 12, justifyContent: "flex-end" }}>
        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
        <button className="btn btn-primary" onClick={submit} disabled={loading || categories.length === 0}>
          {loading ? <Spinner /> : "Create Product"}
        </button>
      </div>
    </Modal>
  );
}

/* ─────────────────────────────────────────────
   ADMIN – DASHBOARD
───────────────────────────────────────────── */
function AdminDashboard({ user, toast }) {
  const [tab, setTab] = useState("categories");
  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [loadingProducts, setLoadingProducts] = useState(false);
  const [showCatModal, setShowCatModal] = useState(false);
  const [showProdModal, setShowProdModal] = useState(false);
  const [selectedCat, setSelectedCat] = useState(null);

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    setLoadingProducts(true);
    try {
      const res = await apiFetch("/api/v1/products?page=0&perPage=100");
      if (res && res.ok) {
        const data = await res.json();
        setProducts(data.products || []);
        // extract unique categories from products
        const catMap = {};
        (data.products || []).forEach((p) => {
          if (p.category) catMap[p.category.categoryId] = { id: p.category.categoryId, name: p.category.categoryName };
        });
        setCategories((prev) => {
          const merged = { ...catMap };
          prev.forEach((c) => { merged[c.id] = c; });
          return Object.values(merged);
        });
      }
    } catch {
      toast("Failed to fetch products", "error");
    } finally {
      setLoadingProducts(false);
    }
  };

  const onCategoryCreated = (cat) => {
    setCategories((prev) => {
      if (prev.find((c) => c.id === cat.id)) return prev;
      return [...prev, cat];
    });
  };

  const filteredProducts = selectedCat
    ? products.filter((p) => p.category?.categoryId === selectedCat)
    : products;

  return (
    <div>
      <div className="welcome-bar">
        <div className="welcome-greeting">Signed in as Admin</div>
        <div className="welcome-name">Welcome back, {user.name || user.preferred_username} 👋</div>
      </div>

      <div className="stat-bar">
        <div className="stat-item">
          <div className="stat-value" style={{ color: "var(--accent)" }}>{categories.length}</div>
          <div className="stat-label">Categories</div>
        </div>
        <div className="stat-item">
          <div className="stat-value" style={{ color: "var(--accent2)" }}>{products.length}</div>
          <div className="stat-label">Products</div>
        </div>
        <div className="stat-item">
          <div className="stat-value" style={{ color: "var(--accent3)" }}>
            {products.filter((p) => p.stockQuantity > 0).length}
          </div>
          <div className="stat-label">In Stock</div>
        </div>
      </div>

      <div className="tabs">
        <button className={`tab-btn ${tab === "categories" ? "active" : ""}`} onClick={() => setTab("categories")}>
          Categories
        </button>
        <button className={`tab-btn ${tab === "products" ? "active" : ""}`} onClick={() => setTab("products")}>
          Products
        </button>
      </div>

      {tab === "categories" && (
        <div>
          <div className="page-header">
            <div>
              <h1 className="page-title">Categories</h1>
              <p className="page-sub">Manage your product taxonomy</p>
            </div>
            <button className="btn btn-primary" onClick={() => setShowCatModal(true)}>
              + New Category
            </button>
          </div>
          {categories.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">🗂</div>
              <div className="empty-title">No categories yet</div>
              <p>Create your first category to organize products</p>
              <button className="btn btn-primary" style={{ marginTop: 20 }} onClick={() => setShowCatModal(true)}>
                Create Category
              </button>
            </div>
          ) : (
            <div>
              {categories.map((c) => (
                <div className="cat-list-item" key={c.id}>
                  <div>
                    <div className="cat-list-name">{c.name}</div>
                    <div className="cat-list-id">ID: {c.id}</div>
                  </div>
                  <div style={{ color: "var(--text-muted)", fontSize: "0.85rem" }}>
                    {products.filter((p) => p.category?.categoryId === c.id).length} products
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {tab === "products" && (
        <div>
          <div className="page-header">
            <div>
              <h1 className="page-title">Products</h1>
              <p className="page-sub">Manage your product catalog</p>
            </div>
            <div style={{ display: "flex", gap: 10 }}>
              <button className="btn btn-secondary btn-sm" onClick={fetchProducts}>↻ Refresh</button>
              <button className="btn btn-primary" onClick={() => setShowProdModal(true)}>
                + New Product
              </button>
            </div>
          </div>

          {categories.length > 0 && (
            <div className="category-pills">
              <button
                className={`category-pill ${!selectedCat ? "active" : ""}`}
                onClick={() => setSelectedCat(null)}
              >All</button>
              {categories.map((c) => (
                <button
                  key={c.id}
                  className={`category-pill ${selectedCat === c.id ? "active" : ""}`}
                  onClick={() => setSelectedCat(selectedCat === c.id ? null : c.id)}
                >
                  {c.name}
                </button>
              ))}
            </div>
          )}

          {loadingProducts ? (
            <div className="loading-center"><Spinner /></div>
          ) : filteredProducts.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">📦</div>
              <div className="empty-title">No products found</div>
              <p>{selectedCat ? "No products in this category" : "Create your first product"}</p>
              <button className="btn btn-primary" style={{ marginTop: 20 }} onClick={() => setShowProdModal(true)}>
                Create Product
              </button>
            </div>
          ) : (
            <div className="grid-3">
              {filteredProducts.map((p) => (
                <div className="product-card" key={p.id} style={{ cursor: "default" }}>
                  <div className="product-name">{p.name}</div>
                  <div className="product-price">₹{p.price?.toFixed(2)}</div>
                  <div className="product-meta">
                    <span className={`product-tag ${p.stockQuantity > 0 ? "tag-stock" : "tag-out"}`}>
                      {p.stockQuantity > 0 ? `${p.stockQuantity} in stock` : "Out of stock"}
                    </span>
                    {p.category && <span className="product-tag tag-cat">{p.category.categoryName}</span>}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {showCatModal && (
        <CreateCategoryModal
          onClose={() => setShowCatModal(false)}
          onSuccess={onCategoryCreated}
          toast={toast}
        />
      )}
      {showProdModal && (
        <CreateProductModal
          categories={categories}
          onClose={() => setShowProdModal(false)}
          onSuccess={(prod) => {
            setProducts((prev) => [prod, ...prev]);
            if (prod.category) {
              setCategories((prev) => {
                if (prev.find((c) => c.id === prod.category.categoryId)) return prev;
                return [...prev, { id: prod.category.categoryId, name: prod.category.categoryName }];
              });
            }
          }}
          toast={toast}
        />
      )}
    </div>
  );
}

/* ─────────────────────────────────────────────
   CUSTOMER – ORDER MODAL
───────────────────────────────────────────── */
function PlaceOrderModal({ onClose, toast }) {
  const [step, setStep] = useState("browse"); // browse | review | success
  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [selectedCat, setSelectedCat] = useState(null);
  const [cart, setCart] = useState({}); // { productId: quantity }
  const [loading, setLoading] = useState(true);
  const [placing, setPlacing] = useState(false);
  const [result, setResult] = useState(null);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const res = await apiFetch("/api/v1/products?page=0&perPage=100");
        if (res && res.ok) {
          const data = await res.json();
          const prods = data.products || [];
          setProducts(prods);
          const catMap = {};
          prods.forEach((p) => {
            if (p.category) catMap[p.category.categoryId] = { id: p.category.categoryId, name: p.category.categoryName };
          });
          setCategories(Object.values(catMap));
        }
      } catch {
        toast("Failed to load products", "error");
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const filteredProducts = selectedCat
    ? products.filter((p) => p.category?.categoryId === selectedCat)
    : products;

  const cartItems = Object.entries(cart)
    .filter(([, qty]) => qty > 0)
    .map(([id, qty]) => ({ product: products.find((p) => p.id === parseInt(id)), qty }))
    .filter((x) => x.product);

  const total = cartItems.reduce((sum, { product, qty }) => sum + product.price * qty, 0);
  const cartCount = cartItems.reduce((s, { qty }) => s + qty, 0);

  const addToCart = (productId, stock) => {
    setCart((c) => {
      const cur = c[productId] || 0;
      if (cur >= stock) return c;
      return { ...c, [productId]: cur + 1 };
    });
  };
  const removeFromCart = (productId) => {
    setCart((c) => {
      const cur = c[productId] || 0;
      if (cur <= 0) return c;
      return { ...c, [productId]: cur - 1 };
    });
  };

  const placeOrder = async () => {
    if (cartItems.length === 0) return toast("Add at least one product", "error");
    setPlacing(true);
    try {
      const res = await apiFetch("/api/v1/orders", {
        method: "POST",
        body: JSON.stringify({
          totalAmount: total,
          orderItems: cartItems.map(({ product, qty }) => ({
            productId: product.id,
            quantity: qty,
            price: product.price,
          })),
        }),
      });
      if (!res) return;
      if (res.ok) {
        const data = await res.json();
        setResult(data);
        setStep("success");
      } else {
        const err = await res.json().catch(() => ({}));
        toast(err.message || "Failed to place order", "error");
      }
    } catch {
      toast("Network error", "error");
    } finally {
      setPlacing(false);
    }
  };

  return (
    <Modal title={step === "success" ? "Order Confirmed" : step === "review" ? "Review Order" : "Browse Products"} onClose={onClose}>
      {step === "success" && result && (
        <div className="success-screen">
          <div className="success-icon">✓</div>
          <div className="success-title">Order Placed!</div>
          <p style={{ color: "var(--text-muted)" }}>{result.message}</p>
          <div className="order-number">#{result.orderNumber}</div>
          <button className="btn btn-secondary" style={{ marginTop: 28 }} onClick={onClose}>Done</button>
        </div>
      )}

      {step === "review" && (
        <div>
          <p style={{ color: "var(--text-muted)", fontSize: "0.875rem", marginBottom: 16 }}>
            Review your items before placing the order.
          </p>
          {cartItems.map(({ product, qty }) => (
            <div className="order-item-row" key={product.id}>
              <div className="order-item-info">
                <div className="order-item-name">{product.name}</div>
                <div className="order-item-price">₹{product.price?.toFixed(2)} each</div>
              </div>
              <div className="qty-control">
                <button className="qty-btn" onClick={() => removeFromCart(product.id)}>−</button>
                <span className="qty-display">{qty}</span>
                <button className="qty-btn" onClick={() => addToCart(product.id, product.stockQuantity)}>+</button>
              </div>
              <div className="order-item-subtotal">₹{(product.price * qty).toFixed(2)}</div>
            </div>
          ))}
          <div className="total-bar">
            <span className="total-label">Total Amount</span>
            <span className="total-value">₹{total.toFixed(2)}</span>
          </div>
          <div style={{ display: "flex", gap: 12, justifyContent: "flex-end" }}>
            <button className="btn btn-secondary" onClick={() => setStep("browse")}>← Back</button>
            <button className="btn btn-success" onClick={placeOrder} disabled={placing || cartItems.length === 0}>
              {placing ? <Spinner /> : "Place Order →"}
            </button>
          </div>
        </div>
      )}

      {step === "browse" && (
        <div>
          {loading ? (
            <div className="loading-center"><Spinner /></div>
          ) : (
            <>
              <div className="category-pills">
                <button className={`category-pill ${!selectedCat ? "active" : ""}`} onClick={() => setSelectedCat(null)}>All</button>
                {categories.map((c) => (
                  <button key={c.id} className={`category-pill ${selectedCat === c.id ? "active" : ""}`}
                    onClick={() => setSelectedCat(selectedCat === c.id ? null : c.id)}>
                    {c.name}
                  </button>
                ))}
              </div>

              {filteredProducts.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-icon">📭</div>
                  <div className="empty-title">No products available</div>
                </div>
              ) : (
                <div style={{ display: "flex", flexDirection: "column", gap: 10, maxHeight: "340px", overflowY: "auto", marginBottom: 16 }}>
                  {filteredProducts.map((p) => {
                    const qty = cart[p.id] || 0;
                    const outOfStock = p.stockQuantity <= 0;
                    return (
                      <div key={p.id} className="order-item-row"
                        style={{ opacity: outOfStock ? 0.5 : 1 }}>
                        <div className="order-item-info">
                          <div className="order-item-name">{p.name}</div>
                          <div className="order-item-price">
                            ₹{p.price?.toFixed(2)} · {p.category?.categoryName}
                            {outOfStock && <span style={{ color: "var(--danger)", marginLeft: 8 }}>Out of stock</span>}
                          </div>
                        </div>
                        <div className="qty-control">
                          <button className="qty-btn" onClick={() => removeFromCart(p.id)} disabled={qty === 0}>−</button>
                          <span className="qty-display">{qty}</span>
                          <button className="qty-btn" onClick={() => addToCart(p.id, p.stockQuantity)} disabled={outOfStock || qty >= p.stockQuantity}>+</button>
                        </div>
                        <div className="order-item-subtotal" style={{ color: qty > 0 ? "var(--accent)" : "var(--text-muted)" }}>
                          {qty > 0 ? `₹${(p.price * qty).toFixed(2)}` : "—"}
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}

              {cartCount > 0 && (
                <div className="total-bar">
                  <span className="total-label">{cartCount} item{cartCount !== 1 ? "s" : ""} selected</span>
                  <span className="total-value">₹{total.toFixed(2)}</span>
                </div>
              )}

              <div style={{ display: "flex", gap: 12, justifyContent: "flex-end" }}>
                <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
                <button className="btn btn-primary" onClick={() => setStep("review")} disabled={cartCount === 0}>
                  Review Order ({cartCount}) →
                </button>
              </div>
            </>
          )}
        </div>
      )}
    </Modal>
  );
}

/* ─────────────────────────────────────────────
   CUSTOMER DASHBOARD
───────────────────────────────────────────── */
function CustomerDashboard({ user, toast }) {
  const [showOrderModal, setShowOrderModal] = useState(false);
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [selectedCat, setSelectedCat] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const res = await apiFetch("/api/v1/products?page=0&perPage=100");
        if (res && res.ok) {
          const data = await res.json();
          const prods = data.products || [];
          setProducts(prods);
          const catMap = {};
          prods.forEach((p) => {
            if (p.category) catMap[p.category.categoryId] = { id: p.category.categoryId, name: p.category.categoryName };
          });
          setCategories(Object.values(catMap));
        }
      } catch {
        toast("Failed to load products", "error");
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const filtered = selectedCat ? products.filter((p) => p.category?.categoryId === selectedCat) : products;

  return (
    <div>
      <div className="welcome-bar">
        <div className="welcome-greeting">Signed in as Customer</div>
        <div className="welcome-name">Hello, {user.name || user.preferred_username} 👋</div>
      </div>

      <div className="page-header">
        <div>
          <h1 className="page-title">Product Catalog</h1>
          <p className="page-sub">{products.length} products available across {categories.length} categories</p>
        </div>
        <button className="btn btn-success" onClick={() => setShowOrderModal(true)}>
          🛒 Place an Order
        </button>
      </div>

      {categories.length > 0 && (
        <div className="category-pills">
          <button className={`category-pill ${!selectedCat ? "active" : ""}`} onClick={() => setSelectedCat(null)}>All</button>
          {categories.map((c) => (
            <button key={c.id} className={`category-pill ${selectedCat === c.id ? "active" : ""}`}
              onClick={() => setSelectedCat(selectedCat === c.id ? null : c.id)}>
              {c.name}
            </button>
          ))}
        </div>
      )}

      {loading ? (
        <div className="loading-center"><Spinner /></div>
      ) : filtered.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">🛍</div>
          <div className="empty-title">No products available</div>
          <p>Check back later — the catalog is being populated.</p>
        </div>
      ) : (
        <div className="grid-3">
          {filtered.map((p) => (
            <div className="product-card" key={p.id} style={{ cursor: "default" }}>
              <div className="product-name">{p.name}</div>
              <div className="product-price">₹{p.price?.toFixed(2)}</div>
              <div className="product-meta">
                <span className={`product-tag ${p.stockQuantity > 0 ? "tag-stock" : "tag-out"}`}>
                  {p.stockQuantity > 0 ? `${p.stockQuantity} in stock` : "Out of stock"}
                </span>
                {p.category && <span className="product-tag tag-cat">{p.category.categoryName}</span>}
              </div>
            </div>
          ))}
        </div>
      )}

      {showOrderModal && (
        <PlaceOrderModal onClose={() => setShowOrderModal(false)} toast={toast} />
      )}
    </div>
  );
}

/* ─────────────────────────────────────────────
   NAVBAR
───────────────────────────────────────────── */
function Navbar({ user, role }) {
  const handleLogout = async () => {
    try {
      //await fetch(`${API_BASE}/logout`, { method: "POST", credentials: "include" });
      window.location.href = `${API_BASE}/do-logout`;
    } catch {}
  };

  return (
    <nav className="navbar">
      <div className="nav-brand">OmniSync</div>
      <div className="nav-right">
        <span className="user-name">{user?.preferred_username || user?.name}</span>
        <span className={`role-badge ${role}`}>{role}</span>
        <button className="logout-btn" onClick={handleLogout}>Sign out</button>
      </div>
    </nav>
  );
}

/* ─────────────────────────────────────────────
   APP ROOT
───────────────────────────────────────────── */
export default function App() {
  const [authState, setAuthState] = useState("loading"); // loading | unauthenticated | admin | customer
  const [user, setUser] = useState(null);
  const { toasts, toast } = useToast();

  useEffect(() => {
    const inject = document.createElement("style");
    inject.textContent = styles;
    document.head.appendChild(inject);
    return () => document.head.removeChild(inject);
  }, []);

  useEffect(() => {
    (async () => {

      // Probe the user-info endpoint through the gateway
      try {
        const res = await fetch(`${API_BASE}/api/me`, {
                  credentials: "include"
                 // redirect: "manual"
                });

        //if (res.status === 401 || res.status === 302 || res.type === "opaqueredirect") {
          //setAuthState("unauthenticated");
          //return;
        //}

        if (res.ok) {
          let userInfo = {};
           userInfo = await res.json();

           if(Object.keys(userInfo).length == 0){
                setAuthState("unauthenticated");
                return;
           }
          console.log(userInfo);

          // Determine role from realm_access if available, else fall back to 'customer'
          let role = "customer";
          const realmRoles = userInfo?.realm_access?.roles || [];
          if (realmRoles.includes("ADMIN")) role = "admin";
          else if (realmRoles.includes("admin")) role = "admin";

          setUser(userInfo);
          setAuthState(role);
          return;
        }

        setAuthState("unauthenticated");
      } catch {
        setAuthState("unauthenticated");
      }
    })();
  }, []);

  if (authState === "loading") {
    return (
      <div style={{ display: "grid", placeItems: "center", minHeight: "100vh", background: "var(--bg)" }}>
        <div style={{ textAlign: "center" }}>
          <div className="spinner" style={{ width: 36, height: 36, borderWidth: 3 }} />
          <p style={{ color: "var(--text-muted)", marginTop: 16 }}>Checking session…</p>
        </div>
      </div>
    );
  }

  if (authState === "unauthenticated") {
    return <AuthPage />;
  }

  return (
    <div className="app-shell">
      <Navbar user={user} role={authState} />
      <main className="main-content">
        {authState === "admin" ? (
          <AdminDashboard user={user} toast={toast} />
        ) : (
          <CustomerDashboard user={user} toast={toast} />
        )}
      </main>
      <ToastContainer toasts={toasts} />
    </div>
  );
}
