const API_BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080/api";

async function get(path) {
  const res = await fetch(`${API_BASE}${path}`);
  if (!res.ok) {
    throw new Error(`${path} failed: ${res.status} ${res.statusText}`);
  }
  return res.json();
}

export function fetchUsers() {
  return get("/users");
}

export function fetchAccess(userId) {
  return get(`/users/${userId}/access`);
}
