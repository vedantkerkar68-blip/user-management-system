import React, { useEffect, useState } from 'react';
import { employeeApi } from '../services/employeeApi';
import { useAuth } from '../context/AuthContext';

const Dashboard = () => {
  const { hasRole } = useAuth();
  const [stats, setStats] = useState(null);
  const [recentEmployees, setRecentEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      const response = await employeeApi.getDashboardStats();
      setStats(response.data);
      setRecentEmployees(response.data.recentEmployees || []);
    } catch (err) {
      if (err.response?.status === 403) {
        setError('Access Denied');
      } else {
        setError('Failed to load dashboard data');
      }
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="container py-5 text-center">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container py-5 text-center">
        <div className="alert alert-warning">
          {error === 'Access Denied' || error.includes('403')
            ? 'You do not have permission to view the dashboard.'
            : error}
        </div>
        <a href="/profile" className="btn btn-primary">Go to My Profile</a>
      </div>
    );
  }

  return (
    <div className="container py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>Dashboard</h2>
      </div>

      <div className="row mb-4">
        <div className="col-md-6 col-lg-3 mb-3">
          <div className="card text-white bg-primary h-100 shadow-sm">
            <div className="card-body">
              <div className="d-flex justify-content-between">
                <div>
                  <h6 className="card-title text-uppercase mb-1">Total Employees</h6>
                  <h2 className="mb-0">{stats?.totalEmployees || 0}</h2>
                </div>
                <div className="align-self-center">
                  <i className="bi bi-people fs-1 opacity-50"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-md-6 col-lg-3 mb-3">
          <div className="card text-white bg-success h-100 shadow-sm">
            <div className="card-body">
              <div className="d-flex justify-content-between">
                <div>
                  <h6 className="card-title text-uppercase mb-1">Active</h6>
                  <h2 className="mb-0">{stats?.activeEmployees || 0}</h2>
                </div>
                <div className="align-self-center">
                  <i className="bi bi-check-circle fs-1 opacity-50"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-md-6 col-lg-3 mb-3">
          <div className="card text-white bg-warning h-100 shadow-sm">
            <div className="card-body">
              <div className="d-flex justify-content-between">
                <div>
                  <h6 className="card-title text-uppercase mb-1">On Leave</h6>
                  <h2 className="mb-0">{stats?.onLeaveEmployees || 0}</h2>
                </div>
                <div className="align-self-center">
                  <i className="bi bi-calendar-x fs-1 opacity-50"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="col-md-6 col-lg-3 mb-3">
          <div className="card text-white bg-danger h-100 shadow-sm">
            <div className="card-body">
              <div className="d-flex justify-content-between">
                <div>
                  <h6 className="card-title text-uppercase mb-1">Inactive/Terminated</h6>
                  <h2 className="mb-0">{(stats?.inactiveEmployees || 0) + (stats?.terminatedEmployees || 0)}</h2>
                </div>
                <div className="align-self-center">
                  <i className="bi bi-x-circle fs-1 opacity-50"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="row">
        <div className="col-lg-7 mb-4">
          <div className="card shadow-sm h-100">
            <div className="card-header">
              <h5 className="mb-0">Department Distribution</h5>
            </div>
            <div className="card-body">
              {stats?.departmentDistribution?.length > 0 ? (
                <div className="table-responsive">
                  <table className="table table-sm">
                    <thead>
                      <tr>
                        <th>Department</th>
                        <th className="text-end">Employees</th>
                      </tr>
                    </thead>
                    <tbody>
                      {stats.departmentDistribution.map(([dept, count], index) => (
                        <tr key={index}>
                          <td>{dept}</td>
                          <td className="text-end fw-bold">{count}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <p className="text-muted text-center mb-0">No department data available</p>
              )}
            </div>
          </div>
        </div>
        <div className="col-lg-5 mb-4">
          <div className="card shadow-sm h-100">
            <div className="card-header">
              <h5 className="mb-0">Recently Joined Employees</h5>
            </div>
            <div className="card-body">
              {recentEmployees.length > 0 ? (
                <div className="list-group list-group-flush">
                  {recentEmployees.map((emp, index) => (
                    <div className="list-group-item px-0 border-0" key={index}>
                      <div className="d-flex justify-content-between">
                        <div>
                          <h6 className="mb-1">{emp.fullName}</h6>
                          <small className="text-muted">{emp.employeeId} - {emp.department}</small>
                        </div>
                        <span className={'badge ' + getStatusBadgeClass(emp.employmentStatus)}>
                          {emp.employmentStatus}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-muted text-center mb-0">No recent employees</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

const getStatusBadgeClass = (status) => {
  switch (status) {
    case 'ACTIVE': return 'bg-success';
    case 'INACTIVE': return 'bg-secondary';
    case 'ON_LEAVE': return 'bg-warning text-dark';
    case 'TERMINATED': return 'bg-danger';
    default: return 'bg-secondary';
  }
};

export default Dashboard;