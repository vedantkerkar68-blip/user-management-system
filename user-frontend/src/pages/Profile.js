import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { employeeApi } from '../services/employeeApi';

const Profile = () => {
  const { user } = useAuth();
  const [employee, setEmployee] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('info');

  useEffect(() => {
    if (user?.id) {
      loadProfile();
    }
  }, [user]);

  const loadProfile = async () => {
    try {
      const response = await employeeApi.getById(user.id);
      setEmployee(response.data);
    } catch (err) {
      setError('Failed to load profile');
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

  return (
    <div className="container py-4">
      <h2 className="mb-4">My Profile</h2>

      {error && <div className="alert alert-danger">{error}</div>}

      <div className="row">
        <div className="col-lg-4">
          <div className="card shadow-sm mb-4">
            <div className="card-body text-center">
              <div className="bg-primary text-white rounded-circle d-inline-flex align-items-center justify-content-center mb-3" style={{ width: '100px', height: '100px' }}>
                <span className="fs-1 fw-bold">{employee?.fullName?.charAt(0) || user?.fullName?.charAt(0) || 'U'}</span>
              </div>
              <h4 className="mb-1">{employee?.fullName || user?.fullName}</h4>
              <p className="text-muted mb-1">{employee?.employeeId || user?.employeeId}</p>
              <p className="text-muted mb-1">{employee?.email || user?.email}</p>
              <span className={'badge ' + getStatusBadgeClass(employee?.employmentStatus || 'ACTIVE') + ' fs-6'}>
                {employee?.employmentStatus || 'ACTIVE'}
              </span>
              <div className="mt-3">
                <span className="badge bg-info text-dark">{employee?.role || user?.role}</span>
              </div>
            </div>
          </div>

          <div className="card shadow-sm">
            <div className="card-header">
              <h5 className="mb-0">Quick Info</h5>
            </div>
            <div className="card-body">
              <ul className="list-group list-group-flush">
                <li className="list-group-item px-0">
                  <strong>Department:</strong> {employee?.department || 'N/A'}
                </li>
                <li className="list-group-item px-0">
                  <strong>Designation:</strong> {employee?.designation || 'N/A'}
                </li>
                <li className="list-group-item px-0">
                  <strong>Joining Date:</strong> {employee?.joiningDate ? new Date(employee.joiningDate).toLocaleDateString() : 'N/A'}
                </li>
                <li className="list-group-item px-0">
                  <strong>Phone:</strong> {employee?.phone || 'Not provided'}
                </li>
              </ul>
            </div>
          </div>
        </div>

        <div className="col-lg-8">
          <div className="card shadow-sm">
            <div className="card-header">
              <ul className="nav nav-tabs card-header-tabs" id="profileTabs" role="tablist">
                <li className="nav-item" role="presentation">
                  <button
                    className={`nav-link ${activeTab === 'info' ? 'active' : ''}`}
                    id="info-tab"
                    data-bs-toggle="tab"
                    data-bs-target="#info"
                    type="button"
                    role="tab"
                    onClick={() => setActiveTab('info')}
                  >
                    Personal Info
                  </button>
                </li>
                <li className="nav-item" role="presentation">
                  <button
                    className={`nav-link ${activeTab === 'employment' ? 'active' : ''}`}
                    id="employment-tab"
                    data-bs-toggle="tab"
                    data-bs-target="#employment"
                    type="button"
                    role="tab"
                    onClick={() => setActiveTab('employment')}
                  >
                    Employment
                  </button>
                </li>
              </ul>
            </div>
            <div className="card-body">
              <div className="tab-content" id="profileTabsContent">
                <div className={`tab-pane fade ${activeTab === 'info' ? 'show active' : ''}`} id="info" role="tabpanel">
                  {employee && (
                    <div className="row g-3">
                      <div className="col-md-6">
                        <label className="form-label text-muted small">Employee ID</label>
                        <p className="fw-bold">{employee.employeeId}</p>
                      </div>
                      <div className="col-md-6">
                        <label className="form-label text-muted small">Full Name</label>
                        <p className="fw-bold">{employee.fullName}</p>
                      </div>
                      <div className="col-md-6">
                        <label className="form-label text-muted small">Email</label>
                        <p>{employee.email}</p>
                      </div>
                      <div className="col-md-6">
                        <label className="form-label text-muted small">Phone</label>
                        <p>{employee.phone || 'Not provided'}</p>
                      </div>
                    </div>
                  )}
                </div>
                <div className={`tab-pane fade ${activeTab === 'employment' ? 'show active' : ''}`} id="employment" role="tabpanel">
                  {employee && (
                    <div className="row g-3">
                      <div className="col-md-6">
                        <label className="form-label text-muted small">Department</label>
                        <p className="fw-bold">{employee.department}</p>
                      </div>
                      <div className="col-md-6">
                        <label className="form-label text-muted small">Designation</label>
                        <p className="fw-bold">{employee.designation}</p>
                      </div>
                      <div className="col-md-6">
                        <label className="form-label text-muted small">Role</label>
                        <p>
                          <span className="badge bg-info text-dark">{employee.role}</span>
                        </p>
                      </div>
                      <div className="col-md-6">
                        <label className="form-label text-muted small">Status</label>
                        <p>
                          <span className={'badge ' + getStatusBadgeClass(employee.employmentStatus)}>
                            {employee.employmentStatus}
                          </span>
                        </p>
                      </div>
                      <div className="col-md-6">
                        <label className="form-label text-muted small">Joining Date</label>
                        <p>{new Date(employee.joiningDate).toLocaleDateString()}</p>
                      </div>
                      <div className="col-md-6">
                        <label className="form-label text-muted small">Created At</label>
                        <p>{employee.createdAt ? new Date(employee.createdAt).toLocaleString() : 'N/A'}</p>
                      </div>
                      <div className="col-md-6">
                        <label className="form-label text-muted small">Last Updated</label>
                        <p>{employee.updatedAt ? new Date(employee.updatedAt).toLocaleString() : 'N/A'}</p>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;