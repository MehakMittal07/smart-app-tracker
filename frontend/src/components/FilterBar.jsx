import { Search } from "lucide-react";

export default function FilterBar({ search, onSearchChange, filterType, onTypeChange, filterStatus, onStatusChange }) {
  return (
    <div style={styles.bar}>
      <div style={styles.searchWrap}>
        <Search size={15} style={styles.searchIcon} />
        <input
          style={styles.input}
          placeholder="Search opportunities..."
          value={search}
          onChange={e => onSearchChange(e.target.value)}
        />
      </div>

      <select style={styles.select} value={filterType} onChange={e => onTypeChange(e.target.value)}>
        <option value="">All types</option>
        <option value="INTERNSHIP">Internship</option>
        <option value="CERTIFICATION">Certification</option>
        <option value="HACKATHON">Hackathon</option>
        <option value="OTHER">Other</option>
      </select>

      <select style={styles.select} value={filterStatus} onChange={e => onStatusChange(e.target.value)}>
        <option value="">All statuses</option>
        <option value="PENDING">Pending</option>
        <option value="APPLIED">Applied</option>
        <option value="SAVED">Saved</option>
        <option value="REJECTED">Rejected</option>
        <option value="EXPIRED">Expired</option>
      </select>
    </div>
  );
}

const styles = {
  bar:        { display: "flex", gap: 10, alignItems: "center", flexWrap: "wrap" },
  searchWrap: { position: "relative", flex: 1, minWidth: 200 },
  searchIcon: { position: "absolute", left: 10, top: "50%", transform: "translateY(-50%)", color: "#aaa" },
  input:      { width: "100%", padding: "8px 12px 8px 34px", borderRadius: 8, border: "1px solid #ddd", fontSize: 14, boxSizing: "border-box" },
  select:     { padding: "8px 12px", borderRadius: 8, border: "1px solid #ddd", fontSize: 14, background: "#fff", cursor: "pointer" },
};