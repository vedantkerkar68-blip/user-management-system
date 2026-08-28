import React, { useEffect, useState } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import { employeeApi } from '../services/employeeApi';
import { useAuth } from '../context/AuthContext';

const EmployeeDetail = () => {
  const { hasRole } = useAuth();
  const { id } = useParams();
  const navigate = useNavigate();
  const [employee, setEmployee] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadEmployee();
  }, [id]);

  const loadEmployee = async () => {
    try {
      const response = await employeeApi.getById(id);
      setEmployee(response.data);
    } catch (err) {
      setError('Employee not found');
      console.error(err);
    } finally {
      setLoading(false);
    }
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

  if (loading) {
    return (
      <div className="container py-5 text-center">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  if (error || !employee) {
    return (
      <div className="container py-5 text-center">
        <div className="alert alert-danger">Employee not found</div>
        <Link to="/employees" className="btn btn-primary">Back to List</Link>
      </div>
    );
  }

  const handleStatusChange = async (status) => {
    if (!window.confirm(`Are you sure you want to change status to ${status}?`)) return;
    try {
      await employeeApi.updateStatus(employee.id, status);
      loadEmployee();
    } catch (err) {
      setError('Failed to update status');
      console.error(err);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this employee? This action cannot be undone.')) return;
    try {
      await employeeApi.delete(employee.id);
      navigate('/employees');
    } catch (err) {
      setError('Failed to delete employee');
      console.error(err);
    }
  };

  return (
    <div className="container py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2>{employee.fullName}</h2>
          <p className="text-muted mb-0">{employee.employeeId} • {employee.email}</p>
        </div>
        <div>
          {hasRole(['ADMIN', 'HR']) && (
            <Link to={`/employees/${employee.id}/edit`} className="btn btn-outline-primary me-2">
              <i className="bi bi-pencil me-1"></i> Edit
            </Link>
          )}
          <Link to="/employees" className="btn btn-outline-secondary">
            <i className="bi bi-arrow-left me-1"></i> Back
          </Link>
        </div>
      </div>

      <div className="row">
        <div className="col-lg-8">
          <div className="card shadow-sm mb-4">
            <div className="card-header">
              <h5 className="mb-0">Personal Information</h5>
            </div>
            <div className="card-body">
              <div className="row mb-3">
                <div className="col-md-6">
                  <label className="form-label text-muted small">Employee ID</label>
                  <p className="mb-0 fw-bold">{employee.employeeId}</p>
                </div>
                <div className="col-md-6">
                  <label className="form-label text-muted small">Full Name</label>
                  <p className="mb-0 fw-bold">{employee.fullName}</p>
                </div>
              </div>
              <div className="row mb-3">
                <div className="col-md-6">
                  <label className="form-label text-muted small">Email</label>
                  <p className="mb-0">{employee.email}</p>
                </div>
                <div className="col-md-6">
                  <label className="form-label text-muted small">Phone</label>
                  <p className="mb-0">{employee.phone || 'Not provided'}</p>
                </div>
              </div>
              <div className="row mb-3">
                <div className="col-md-6">
                  <label className="form-label text-muted small">Department</label>
                  <p className="mb-0">{employee.department}</p>
                </div>
                <div className="col-md-6">
                  <label className="form-label text-muted small">Designation</label>
                  <p className="mb-0">{employee.designation}</p>
                </div>
              </div>
            </div>
          </div>

          <div className="card shadow-sm mb-4">
            <div className="card-header">
              <h5 className="mb-0">Employment Details</h5>
            </div>
            <div className="card-body">
              <div className="row mb-3">
                <div className="col-md-4">
                  <label className="form-label text-muted small">Role</label>
                  <p className="mb-0">
                    <span className="badge bg-info text-dark">{employee.role}</span>
                  </p>
                </div>
                <div className="col-md-4">
                  <label className="form-label text-muted small">Employment Status</label>
                  <p className="mb-0">
                    <span className={'badge ' + getStatusBadgeClass(employee.employmentStatus)}>
                      {employee.employmentStatus}
                    </span>
                  </p>
                </div>
                <div className="col-md-4">
                  <label className="form-label text-muted small">Joining Date</label>
                  <p className="mb-0">{new Date(employee.joiningDate).toLocaleDateString()}</p>
                </div>
              </div>
            </div>
          </div>

          <div className="card shadow-sm">
            <div className="card-header">
              <h5 className="mb-0">System Information</h5>
            </div>
            <div className="card-body">
              <div className="row mb-3">
                <div className="col-md-6">
                  <label className="form-label text-muted small">Created At</label>
                  <p className="mb-0">{employee.createdAt ? new Date(employee.createdAt).toLocaleString() : 'N/A'}</p>
                </div>
                <div className="col-md-6">
                  <label className="form-label text-muted small">Last Updated</label>
                  <p className="mb-0">{employee.updatedAt ? new Date(employee.updatedAt).toLocaleString() : 'N/A'}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="col-lg-4">
          {hasRole(['ADMIN', 'HR']) && (
            <div className="card shadow-sm sticky-top" style={{ top: '20px' }}>
              <div className="card-header">
                <h5 className="mb-0">Quick Actions</h5>
              </div>
              <div className="card-body">
                <div className="d-grid gap-2">
                  <button
                    className="btn btn-outline-primary"
                    onClick={() => navigate(`/employees/${employee.id}/edit`)}
                  >
                    <i className="bi bi-pencil me-1"></i> Edit Employee
                  </button>
                  <div className="dropdown">
                    <button
                      className="btn btn-outline-warning dropdown-toggle"
                      type="button"
                      data-bs-toggle="dropdown"
                      aria-expanded="false"
                    >
                      <i className="bi bi-person-check me-1"></i> Change Status
                    </button>
                    <ul className="dropdown-menu w-100">
                      <li>
                        <button
                          className="dropdown-item"
                          onClick={() => handleStatusChange('ACTIVE')}
                        >
                          <i className="bi bi-check-circle text-success me-2"></i> Activate
                        </button>
                      </li>
                      <li>
                        <button
                          className="dropdown-item"
                          onClick={() => handleStatusChange('INACTIVE')}
                        >
                          <i className="bi bi-pause-circle text-secondary me-2"></i> Deactivate
                        </button>
                      </li>
                      <li>
                        <button
                          className="dropdown-item"
                          onClick={() => handleStatusChange('ON_LEAVE')}
                        >
                          <i className="bi bi-calendar-x text-warning me-2"></i> On Leave
                        </button>
                      </li>
                      <li>
                        <button
                          className="dropdown-item text-danger"
                          onClick={() => handleStatusChange('TERMINATED')}
                        >
                          <i className="bi bi-x-circle me-2"></i> Terminate
                        </button>
                      </li>
                    </ul>
                  </div>
                  {hasRole(['ADMIN']) && (
                    <button
                      className="btn btn-outline-danger"
                      onClick={() => handleDelete()}
                    >
                      <i className="bi bi-trash me-1"></i> Delete Employee
                    </button>
                  )}
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default EmployeeDetail;