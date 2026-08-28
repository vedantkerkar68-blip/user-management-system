import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout, hasRole, loading } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (loading) {
    return (
      <nav className="navbar navbar-expand-lg navbar-dark bg-primary">
        <div className="container-fluid">
          <span className="navbar-brand">Workforce Management System</span>
          <div className="spinner-border spinner-border-sm text-light ms-auto" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      </nav>
    );
  }

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-primary">
      <div className="container-fluid">
        <Link className="navbar-brand" to="/">
          Workforce Management System
        </Link>
        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarSupportedContent"
          aria-controls="navbarSupportedContent"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        <div className="collapse navbar-collapse" id="navbarSupportedContent">
          <ul className="navbar-nav me-auto mb-2 mb-lg-0">
            {hasRole(['ADMIN', 'HR', 'MANAGER']) && (
              <li className="nav-item">
                <Link className="nav-link" to="/">Dashboard</Link>
              </li>
            )}
            {hasRole(['ADMIN', 'HR', 'MANAGER']) && (
              <li className="nav-item">
                <Link className="nav-link" to="/employees">Employees</Link>
              </li>
            )}
            {hasRole(['ADMIN', 'HR']) && (
              <li className="nav-item">
                <Link className="nav-link" to="/employees/add">Add Employee</Link>
              </li>
            )}
            {hasRole(['ADMIN']) && (
              <li className="nav-item">
                <Link className="nav-link" to="/audit-logs">Audit Logs</Link>
              </li>
            )}
            {hasRole(['EMPLOYEE']) && !hasRole(['ADMIN', 'HR', 'MANAGER']) && (
              <li className="nav-item">
                <Link className="nav-link" to="/profile">My Profile</Link>
              </li>
            )}
          </ul>

          <div className="d-flex align-items-center gap-3">
            {user && (
              <>
                <span className="navbar-text text-white me-3">
                  <i className="bi bi-person-circle me-1"></i>
                  {user.fullName} <span className="badge bg-light text-dark ms-1">{user.role}</span>
                </span>
                <Link to="/profile" className="btn btn-outline-light btn-sm me-2">
                  <i className="bi bi-person me-1"></i> Profile
                </Link>
                <button
                  className="btn btn-outline-light btn-sm"
                  onClick={handleLogout}
                >
                  <i className="bi bi-box-arrow-right me-1"></i> Logout
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}