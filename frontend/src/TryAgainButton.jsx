import React from "react";

export default function TryAgainButton({ onClick }) {
  return (
    <button
      onClick={onClick}
      style={{
        marginTop: "1rem",
        padding: "0.5rem 1rem",
        fontSize: "0.9rem",
        backgroundColor: "#007BFF",
        color: "white",
        border: "none",
        borderRadius: "4px",
        cursor: "pointer",
        transition: "background-color 0.2s",
      }}
      onMouseEnter={(e) => (e.target.style.backgroundColor = "#0056b3")}
      onMouseLeave={(e) => (e.target.style.backgroundColor = "#007BFF")}
    >
      Go Again!
    </button>
  );
}
