import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { employeeApi } from '../services/employeeApi';
import { useAuth } from '../context/AuthContext';

const EmployeeList = () => {
  const { hasRole } = useAuth();
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [pagination, setPagination] = useState({ page: 0, size: 10, totalElements: 0, totalPages: 0 });
  const [filters, setFilters] = useState({
    search: '',
    department: '',
    role: '',
    status: '',
    sortBy: 'fullName',
    sortDir: 'asc',
  });
  const [departments, setDepartments] = useState([]);

  useEffect(() => {
    loadEmployees();
    loadDepartments();
  }, [pagination.page, pagination.size, filters]);

  const loadEmployees = async () => {
    setLoading(true);
    try {
      const params = {
        page: pagination.page,
        size: pagination.size,
        sortBy: filters.sortBy,
        sortDir: filters.sortDir,
        ...filters,
      };
      const response = await employeeApi.getAll(params);
      setEmployees(response.data.content);
      setPagination(prev => ({
        ...prev,
        totalElements: response.data.totalElements,
        totalPages: response.data.totalPages,
      }));
    } catch (err) {
      setError('Failed to load employees');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const loadDepartments = async () => {
    try {
      const response = await employeeApi.getDepartments();
      setDepartments(response.data);
    } catch (err) {
      console.error(err);
    }
  };

  const handleFilterChange = (name, value) => {
    setFilters(prev => ({ ...prev, [name]: value }));
    setPagination(prev => ({ ...prev, page: 0 }));
  };

  const handleSort = (sortBy) => {
    setFilters(prev => ({
      ...prev,
      sortBy,
      sortDir: prev.sortBy === sortBy && prev.sortDir === 'asc' ? 'desc' : 'asc',
    }));
  };

  const handlePageChange = (newPage) => {
    setPagination(prev => ({ ...prev, page: newPage }));
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this employee?')) return;
    try {
      await employeeApi.delete(id);
      loadEmployees();
    } catch (err) {
      setError('Failed to delete employee');
      console.error(err);
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

  return (
    <div className="container py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>Employees</h2>
        {hasRole(['ADMIN', 'HR']) && (
          <Link to="/employees/add" className="btn btn-primary">
            <i className="bi bi-plus-lg me-1"></i> Add Employee
          </Link>
        )}
      </div>

      {error && <div className="alert alert-danger alert-dismissible fade show" role="alert">
        {error}
        <button type="button" className="btn-close" onClick={() => setError('')}></button>
      </div>}

      <div className="card shadow-sm mb-4">
        <div className="card-header">
          <h5 className="mb-0">Filters</h5>
        </div>
        <div className="card-body">
          <div className="row g-3">
            <div className="col-md-4">
              <label className="form-label">Search</label>
              <input
                type="text"
                className="form-control"
                placeholder="Search by ID, name, email..."
                value={filters.search}
                onChange={(e) => handleFilterChange('search', e.target.value)}
              />
            </div>
            <div className="col-md-2">
              <label className="form-label">Department</label>
              <select
                className="form-select"
                value={filters.department}
                onChange={(e) => handleFilterChange('department', e.target.value)}
              >
                <option value="">All Departments</option>
                {departments.map(dept => (
                  <option key={dept} value={dept}>{dept}</option>
                ))}
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label">Role</label>
              <select
                className="form-select"
                value={filters.role}
                onChange={(e) => handleFilterChange('role', e.target.value)}
              >
                <option value="">All Roles</option>
                <option value="ADMIN">Admin</option>
                <option value="HR">HR</option>
                <option value="MANAGER">Manager</option>
                <option value="EMPLOYEE">Employee</option>
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label">Status</label>
              <select
                className="form-select"
                value={filters.status}
                onChange={(e) => handleFilterChange('status', e.target.value)}
              >
                <option value="">All Statuses</option>
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
                <option value="ON_LEAVE">On Leave</option>
                <option value="TERMINATED">Terminated</option>
              </select>
            </div>
            <div className="col-md-2 d-flex align-items-end">
              <button
                className="btn btn-outline-secondary w-100"
                onClick={() => setFilters({ search: '', department: '', role: '', status: '', sortBy: 'fullName', sortDir: 'asc' })}
              >
                Clear Filters
              </button>
            </div>
          </div>
        </div>
      </div>

      <div className="card shadow-sm">
        <div className="card-body p-0">
          {loading ? (
            <div className="text-center py-5">
              <div className="spinner-border text-primary" role="status">
                <span className="visually-hidden">Loading...</span>
              </div>
            </div>
          ) : employees.length === 0 ? (
            <div className="text-center py-5">
              <p className="text-muted mb-0">No employees found</p>
            </div>
          ) : (
            <div>
              <div className="table-responsive">
                <table className="table table-hover mb-0">
                  <thead className="table-light">
                    <tr>
                      <th>
                        <a href="#" className="text-decoration-none text-dark" onClick={(e) => { e.preventDefault(); handleSort('employeeId'); }}>
                          Employee ID {filters.sortBy === 'employeeId' && (filters.sortDir === 'asc' ? '↑' : '↓')}
                        </a>
                      </th>
                      <th>
                        <a href="#" className="text-decoration-none text-dark" onClick={(e) => { e.preventDefault(); handleSort('fullName'); }}>
                          Name {filters.sortBy === 'fullName' && (filters.sortDir === 'asc' ? '↑' : '↓')}
                        </a>
                      </th>
                      <th>
                        <a href="#" className="text-decoration-none text-dark" onClick={(e) => { e.preventDefault(); handleSort('email'); }}>
                          Email {filters.sortBy === 'email' && (filters.sortDir === 'asc' ? '↑' : '↓')}
                        </a>
                      </th>
                      <th>Department</th>
                      <th>Designation</th>
                      <th>Role</th>
                      <th>Status</th>
                      <th>
                        <a href="#" className="text-decoration-none text-dark" onClick={(e) => { e.preventDefault(); handleSort('joiningDate'); }}>
                          Joining Date {filters.sortBy === 'joiningDate' && (filters.sortDir === 'asc' ? '↑' : '↓')}
                        </a>
                      </th>
                      <th className="text-center">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {employees.map((employee) => (
                      <tr key={employee.id}>
                        <td>{employee.employeeId}</td>
                        <td>{employee.fullName}</td>
                        <td>{employee.email}</td>
                        <td>{employee.department}</td>
                        <td>{employee.designation}</td>
                        <td><span className="badge bg-info text-dark">{employee.role}</span></td>
                        <td>
                          <span className={'badge ' + getStatusBadgeClass(employee.employmentStatus)}>
                            {employee.employmentStatus}
                          </span>
                        </td>
                        <td>{new Date(employee.joiningDate).toLocaleDateString()}</td>
                        <td className="text-center">
                          <div className="btn-group btn-group-sm">
                            <Link to={`/employees/${employee.id}`} className="btn btn-outline-primary" title="View">
                              <i className="bi bi-eye"></i>
                            </Link>
                            {hasRole(['ADMIN', 'HR']) && (
                              <Link to={`/employees/${employee.id}/edit`} className="btn btn-outline-secondary" title="Edit">
                                <i className="bi bi-pencil"></i>
                              </Link>
                            )}
                            {hasRole(['ADMIN']) && (
                              <button
                                className="btn btn-outline-danger"
                                title="Delete"
                                onClick={() => handleDelete(employee.id)}
                              >
                                <i className="bi bi-trash"></i>
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              {pagination.totalPages > 1 && (
                <div className="card-footer bg-white border-top">
                  <div>
                    <nav aria-label="Employee pagination">
                      <ul className="pagination pagination-sm justify-content-center mb-0">
                        <li className={'page-item ' + (pagination.page === 0 ? 'disabled' : '')}>
                          <button
                            className="page-link"
                            onClick={() => handlePageChange(0)}
                            disabled={pagination.page === 0}
                          >
                            &laquo; First
                          </button>
                        </li>
                        <li className={'page-item ' + (pagination.page === 0 ? 'disabled' : '')}>
                          <button
                            className="page-link"
                            onClick={() => handlePageChange(pagination.page - 1)}
                            disabled={pagination.page === 0}
                          >
                            Previous
                          </button>
                        </li>
                        {Array.from({ length: Math.min(5, pagination.totalPages) }, (_, i) => {
                          let pageNum;
                          if (pagination.totalPages <= 5) {
                            pageNum = i;
                          } else if (pagination.page <= 2) {
                            pageNum = i;
                          } else if (pagination.page >= pagination.totalPages - 3) {
                            pageNum = pagination.totalPages - 5 + i;
                          } else {
                            pageNum = pagination.page - 2 + i;
                          }
                          return (
                            <li key={pageNum} className={'page-item ' + (pageNum === pagination.page ? 'active' : '')}>
                              <button
                                className="page-link"
                                onClick={() => handlePageChange(pageNum)}
                              >
                                {pageNum + 1}
                              </button>
                            </li>
                          );
                        })}
                        <li className={'page-item ' + (pagination.page === pagination.totalPages - 1 ? 'disabled' : '')}>
                          <button
                            className="page-link"
                            onClick={() => handlePageChange(pagination.page + 1)}
                            disabled={pagination.page === pagination.totalPages - 1}
                          >
                            Next
                          </button>
                        </li>
                        <li className={'page-item ' + (pagination.page === pagination.totalPages - 1 ? 'disabled' : '')}>
                          <button
                            className="page-link"
                            onClick={() => handlePageChange(pagination.totalPages - 1)}
                            disabled={pagination.page === pagination.totalPages - 1}
                          >
                            Last &raquo;
                          </button>
                        </li>
                      </ul>
                    </nav>
                    <p className="text-muted small text-center mb-0">
                      Showing {pagination.page * pagination.size + 1} to {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)} of {pagination.totalElements} employees
                    </p>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default EmployeeList;