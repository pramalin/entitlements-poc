import { useEffect, useState } from "react";
import { fetchUsers, fetchAccess } from "./api.js";
import Tooltip from "./Tooltip.jsx";

export default function App() {
  const [users, setUsers] = useState([]);
  const [selectedUserId, setSelectedUserId] = useState(null);
  const [access, setAccess] = useState([]);
  const [loadingUsers, setLoadingUsers] = useState(true);
  const [loadingAccess, setLoadingAccess] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchUsers()
      .then((data) => {
        setUsers(data);
        if (data.length) setSelectedUserId(data[0].userId);
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoadingUsers(false));
  }, []);

  useEffect(() => {
    if (selectedUserId == null) return;
    setLoadingAccess(true);
    fetchAccess(selectedUserId)
      .then(setAccess)
      .catch((e) => setError(e.message))
      .finally(() => setLoadingAccess(false));
  }, [selectedUserId]);

  const selectedUser = users.find((u) => u.userId === selectedUserId);

  return (
    <>
      <header className="topbar">
        <div className="brand">
          <span className="brand-mark">◆</span>
          <div>
            <div className="brand-title">Entitlement Access Viewer</div>
            <div className="brand-sub">
              Access descriptions generated offline · no live LLM calls at
              view time
            </div>
          </div>
        </div>
      </header>

      <main className="layout">
        <aside className="user-pane">
          <div className="pane-label">Users</div>
          {error && <div className="error-banner">{error}</div>}
          <ul className="user-list">
            {loadingUsers && <li className="user-list-loading">Loading…</li>}
            {users.map((u) => (
              <li
                key={u.userId}
                className={u.userId === selectedUserId ? "active" : ""}
                onClick={() => setSelectedUserId(u.userId)}
              >
                <div className="user-name">{u.fullName}</div>
                <div className="user-meta">{u.title}</div>
              </li>
            ))}
          </ul>
        </aside>

        <section className="access-pane">
          <div className="access-header">
            {selectedUser ? (
              <>
                <div className="who">{selectedUser.fullName}</div>
                <div className="who-meta">
                  {selectedUser.title} · {selectedUser.department} · reports
                  to {selectedUser.managerName}
                </div>
              </>
            ) : (
              <span className="placeholder">
                Select a user to view their access
              </span>
            )}
          </div>

          <table className="access-table">
            <thead>
              <tr>
                <th>Application</th>
                <th>Access Title</th>
                <th>Type</th>
                <th>Granted</th>
              </tr>
            </thead>
            <tbody>
              {loadingAccess && (
                <tr>
                  <td colSpan={4} className="loading-row">
                    Loading access…
                  </td>
                </tr>
              )}
              {!loadingAccess &&
                access.map((row) => (
                  <tr key={row.entitlementId}>
                    <td>{row.applicationName}</td>
                    <td>
                      <Tooltip text={row.description} riskNote={row.riskNote}>
                        <span
                          className={
                            row.riskNote
                              ? "title-cell flagged"
                              : "title-cell"
                          }
                        >
                          {row.crypticTitle}
                        </span>
                      </Tooltip>
                    </td>
                    <td>
                      <span className="type-pill">
                        {row.entitlementType || "-"}
                      </span>
                    </td>
                    <td>{row.grantedDate || ""}</td>
                  </tr>
                ))}
            </tbody>
          </table>
        </section>
      </main>
    </>
  );
}
