import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { employeeApi } from '../services/employeeApi';

const AddEmployee = () => {
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
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const validateForm = () => {
    const newErrors = {};
    if (!formData.employeeId.trim()) newErrors.employeeId = 'Employee ID is required';
    else if (!/^[A-Z0-9-]+$/.test(formData.employeeId)) newErrors.employeeId = 'Employee ID must contain only uppercase letters, numbers, and hyphens';
    else if (formData.employeeId.length < 3 || formData.employeeId.length > 20) newErrors.employeeId = 'Employee ID must be between 3 and 20 characters';

    if (!formData.fullName.trim()) newErrors.fullName = 'Full name is required';
    else if (formData.fullName.length < 2 || formData.fullName.length > 100) newErrors.fullName = 'Full name must be between 2 and 100 characters';

    if (!formData.email.trim()) newErrors.email = 'Email is required';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) newErrors.email = 'Invalid email format';

    if (!formData.password.trim()) newErrors.password = 'Password is required';
    else if (formData.password.length < 6) newErrors.password = 'Password must be at least 6 characters';

    if (formData.phone && !/^[0-9]{10}$/.test(formData.phone)) newErrors.phone = 'Phone must be exactly 10 digits';

    if (!formData.department.trim()) newErrors.department = 'Department is required';
    else if (formData.department.length < 2 || formData.department.length > 100) newErrors.department = 'Department must be between 2 and 100 characters';

    if (!formData.designation.trim()) newErrors.designation = 'Designation is required';
    else if (formData.designation.length < 2 || formData.designation.length > 100) newErrors.designation = 'Designation must be between 2 and 100 characters';

    if (!formData.joiningDate) newErrors.joiningDate = 'Joining date is required';

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setLoading(true);
    setError('');
    try {
      await employeeApi.create(formData);
      navigate('/employees');
    } catch (err) {
      if (err.response?.data?.path) {
        setErrors(prev => ({ ...prev, submit: err.response.data.message }));
      } else {
        setError(err.response?.data?.message || 'Failed to create employee');
      }
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>Add Employee</h2>
        <a href="/employees" className="btn btn-outline-secondary">
          <i className="bi bi-arrow-left me-1"></i> Back to List
        </a>
      </div>

      {error && <div className="alert alert-danger alert-dismissible fade show" role="alert">
        {error}
        <button type="button" className="btn-close" onClick={() => setError('')}></button>
      </div>}

      <div className="card shadow-sm">
        <div className="card-body p-4">
          <form onSubmit={handleSubmit}>
            <div className="row g-3">
              <div className="col-md-6">
                <label htmlFor="employeeId" className="form-label">Employee ID <span className="text-danger">*</span></label>
                <input
                  type="text"
                  className={`form-control ${errors.employeeId ? 'is-invalid' : ''}`}
                  id="employeeId"
                  name="employeeId"
                  value={formData.employeeId}
                  onChange={handleChange}
                  placeholder="EMP-001"
                  required
                />
                {errors.employeeId && <div className="invalid-feedback">{errors.employeeId}</div>}
              </div>
              <div className="col-md-6">
                <label htmlFor="fullName" className="form-label">Full Name <span className="text-danger">*</span></label>
                <input
                  type="text"
                  className={`form-control ${errors.fullName ? 'is-invalid' : ''}`}
                  id="fullName"
                  name="fullName"
                  value={formData.fullName}
                  onChange={handleChange}
                  required
                />
                {errors.fullName && <div className="invalid-feedback">{errors.fullName}</div>}
              </div>
              <div className="col-md-6">
                <label htmlFor="email" className="form-label">Email <span className="text-danger">*</span></label>
                <input
                  type="email"
                  className={`form-control ${errors.email ? 'is-invalid' : ''}`}
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                />
                {errors.email && <div className="invalid-feedback">{errors.email}</div>}
              </div>
              <div className="col-md-6">
                <label htmlFor="password" className="form-label">Password <span className="text-danger">*</span></label>
                <input
                  type="password"
                  className={`form-control ${errors.password ? 'is-invalid' : ''}`}
                  id="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  minLength={6}
                />
                {errors.password && <div className="invalid-feedback">{errors.password}</div>}
              </div>
              <div className="col-md-6">
                <label htmlFor="phone" className="form-label">Phone</label>
                <input
                  type="tel"
                  className={`form-control ${errors.phone ? 'is-invalid' : ''}`}
                  id="phone"
                  name="phone"
                  value={formData.phone}
                  onChange={handleChange}
                  placeholder="10 digits"
                />
                {errors.phone && <div className="invalid-feedback">{errors.phone}</div>}
              </div>
              <div className="col-md-6">
                <label htmlFor="department" className="form-label">Department <span className="text-danger">*</span></label>
                <input
                  type="text"
                  className={`form-control ${errors.department ? 'is-invalid' : ''}`}
                  id="department"
                  name="department"
                  value={formData.department}
                  onChange={handleChange}
                  required
                />
                {errors.department && <div className="invalid-feedback">{errors.department}</div>}
              </div>
              <div className="col-md-6">
                <label htmlFor="designation" className="form-label">Designation <span className="text-danger">*</span></label>
                <input
                  type="text"
                  className={`form-control ${errors.designation ? 'is-invalid' : ''}`}
                  id="designation"
                  name="designation"
                  value={formData.designation}
                  onChange={handleChange}
                  required
                />
                {errors.designation && <div className="invalid-feedback">{errors.designation}</div>}
              </div>
              <div className="col-md-6">
                <label htmlFor="role" className="form-label">Role <span className="text-danger">*</span></label>
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
              <div className="col-md-6">
                <label htmlFor="joiningDate" className="form-label">Joining Date <span className="text-danger">*</span></label>
                <input
                  type="date"
                  className={`form-control ${errors.joiningDate ? 'is-invalid' : ''}`}
                  id="joiningDate"
                  name="joiningDate"
                  value={formData.joiningDate}
                  onChange={handleChange}
                  required
                />
                {errors.joiningDate && <div className="invalid-feedback">{errors.joiningDate}</div>}
              </div>
            </div>
            <div className="d-flex justify-content-end gap-2 mt-4">
              <a href="/employees" className="btn btn-outline-secondary">
                Cancel
              </a>
              <button
                type="submit"
                className="btn btn-primary"
                disabled={loading}
              >
                {loading ? 'Creating...' : 'Create Employee'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AddEmployee;