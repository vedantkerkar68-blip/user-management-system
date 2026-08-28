import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Register = () => {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    employeeId: '',
    fullName: '',
    email: '',
    password: '',
    phone: '',
    department: '',
    designation: '',
    role: 'EMPLOYEE',
    joiningDate: new Date().toISOString().split('T')[0],
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await register(formData);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container">
      <div className="row justify-content-center mt-5">
        <div className="col-md-8 col-lg-6">
          <div className="card shadow">
            <div className="card-header bg-primary text-white text-center">
              <h3>Workforce Management System</h3>
              <p className="mb-0">Create your account</p>
            </div>
            <div className="card-body p-4">
              {error && (
                <div className="alert alert-danger" role="alert">
                  {error}
                </div>
              )}
              <form onSubmit={handleSubmit}>
                <div className="row">
                  <div className="col-md-6 mb-3">
                    <label htmlFor="employeeId" className="form-label">Employee ID</label>
                    <input
                      type="text"
                      className="form-control"
                      id="employeeId"
                      name="employeeId"
                      value={formData.employeeId}
                      onChange={handleChange}
                      required
                      placeholder="EMP-001"
                    />
                  </div>
                  <div className="col-md-6 mb-3">
                    <label htmlFor="fullName" className="form-label">Full Name</label>
                    <input
                      type="text"
                      className="form-control"
                      id="fullName"
                      name="fullName"
                      value={formData.fullName}
                      onChange={handleChange}
                      required
                    />
                  </div>
                </div>
                <div className="mb-3">
                  <label htmlFor="email" className="form-label">Email</label>
                  <input
                    type="email"
                    className="form-control"
                    id="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div className="mb-3">
                  <label htmlFor="password" className="form-label">Password</label>
                  <input
                    type="password"
                    className="form-control"
                    id="password"
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    required
                    minLength={6}
                  />
                </div>
                <div className="row">
                  <div className="col-md-6 mb-3">
                    <label htmlFor="phone" className="form-label">Phone</label>
                    <input
                      type="tel"
                      className="form-control"
                      id="phone"
                      name="phone"
                      value={formData.phone}
                      onChange={handleChange}
                      placeholder="10 digits"
                    />
                  </div>
                  <div className="col-md-6 mb-3">
                    <label htmlFor="department" className="form-label">Department</label>
                    <input
                      type="text"
                      className="form-control"
                      id="department"
                      name="department"
                      value={formData.department}
                      onChange={handleChange}
                      required
                    />
                  </div>
                </div>
                <div className="row">
                  <div className="col-md-6 mb-3">
                    <label htmlFor="designation" className="form-label">Designation</label>
                    <input
                      type="text"
                      className="form-control"
                      id="designation"
                      name="designation"
                      value={formData.designation}
                      onChange={handleChange}
                      required
                    />
                  </div>
                  <div className="col-md-6 mb-3">
                    <label htmlFor="role" className="form-label">Role</label>
                    <select
                      className="form-select"
                      id="role"
                      name="role"
                      value={formData.role}
                      onChange={handleChange}
                      required
                    >
                      <option value="EMPLOYEE">Employee</option>
                      <option value="MANAGER">Manager</option>
                      <option value="HR">HR</option>
                      <option value="ADMIN">Admin</option>
                    </select>
                  </div>
                </div>
                <div className="mb-3">
                  <label htmlFor="joiningDate" className="form-label">Joining Date</label>
                  <input
                    type="date"
                    className="form-control"
                    id="joiningDate"
                    name="joiningDate"
                    value={formData.joiningDate}
                    onChange={handleChange}
                    required
                  />
                </div>
                <button
                  type="submit"
                  className="btn btn-primary w-100"
                  disabled={loading}
                >
                  {loading ? 'Registering...' : 'Register'}
                </button>
              </form>
              <div className="text-center mt-3">
                <Link to="/login" className="text-decoration-none">
                  Already have an account? Sign in
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;