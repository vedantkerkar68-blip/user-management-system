import React, { useEffect, useState } from 'react';
import { auditApi } from '../services/auditApi';
import { useAuth } from '../context/AuthContext';

const AuditLogs = () => {
  const { hasRole } = useAuth();
  const [logs, setLogs] = useState([]);
  const [stats, setStats] = useState(null);
  const [actions, setActions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [pagination, setPagination] = useState({ page: 0, size: 20, totalElements: 0, totalPages: 0 });
  const [filters, setFilters] = useState({
    actorId: '',
    action: '',
    startDate: '',
    endDate: '',
    sortBy: 'timestamp',
    sortDir: 'desc',
  });

  useEffect(() => {
    if (hasRole(['ADMIN'])) {
      loadLogs();
      loadStats();
      loadActions();
    }
  }, [pagination.page, pagination.size, filters]);

  const loadLogs = async () => {
    setLoading(true);
    try {
      const params = {
        page: pagination.page,
        size: pagination.size,
        sortBy: filters.sortBy,
        sortDir: filters.sortDir,
        ...filters,
      };
      Object.keys(params).forEach(key => {
        if (params[key] === '' || params[key] === null || params[key] === undefined) {
          delete params[key];
        }
      });
      const response = await auditApi.getAll(params);
      setLogs(response.data.content);
      setPagination(prev => ({
        ...prev,
        totalElements: response.data.totalElements,
        totalPages: response.data.totalPages,
      }));
    } catch (err) {
      setError('Failed to load audit logs');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const loadStats = async () => {
    try {
      const response = await auditApi.getStats();
      setStats(response.data);
    } catch (err) {
      console.error(err);
    }
  };

  const loadActions = async () => {
    try {
      const response = await auditApi.getActions();
      setActions(response.data);
    } catch (err) {
      console.error(err);
    }
  };

  const handleFilterChange = (name, value) => {
    setFilters(prev => ({ ...prev, [name]: value }));
    setPagination(prev => ({ ...prev, page: 0 }));
  };

  const handlePageChange = (newPage) => {
    setPagination(prev => ({ ...prev, page: newPage }));
  };

  const getActionBadgeClass = (action) => {
    if (action.includes('FAILED') || action.includes('TERMINATE')) return 'bg-danger';
    if (action.includes('CREATE') || action.includes('ACTIVATE')) return 'bg-success';
    if (action.includes('UPDATE') || action.includes('CHANGE')) return 'bg-warning text-dark';
    if (action.includes('DEACTIVATE')) return 'bg-secondary';
    return 'bg-info';
  };

  if (!hasRole(['ADMIN'])) {
    return (
      <div className="container py-5 text-center">
        <div className="alert alert-danger">Access denied. Admin role required.</div>
      </div>
    );
  }

  const renderPagination = () => {
    if (pagination.totalPages <= 1) return null;
    return (
      <div className="card-footer bg-white border-top">
        <div>
          <nav aria-label="Audit log pagination">
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
            Showing {pagination.page * pagination.size + 1} to {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)} of {pagination.totalElements} logs
          </p>
        </div>
      </div>
    );
  };

  return (
    <div className="container py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>Audit Logs</h2>
      </div>

      {stats && (
        <div className="row mb-4">
          <div className="col-md-3 mb-3">
            <div className="card text-white bg-primary h-100 shadow-sm">
              <div className="card-body">
                <h6 className="card-title text-uppercase mb-1">Total Logs</h6>
                <h2 className="mb-0">{stats.totalLogs}</h2>
              </div>
            </div>
          </div>
          <div className="col-md-3 mb-3">
            <div className="card text-white bg-success h-100 shadow-sm">
              <div className="card-body">
                <h6 className="card-title text-uppercase mb-1">Last 24 Hours</h6>
                <h2 className="mb-0">{stats.logsLast24Hours}</h2>
              </div>
            </div>
          </div>
          <div className="col-md-3 mb-3">
            <div className="card text-white bg-warning h-100 shadow-sm">
              <div className="card-body">
                <h6 className="card-title text-uppercase mb-1">Last 7 Days</h6>
                <h2 className="mb-0">{stats.logsLast7Days}</h2>
              </div>
            </div>
          </div>
          <div className="col-md-3 mb-3">
            <div className="card text-white bg-danger h-100 shadow-sm">
              <div className="card-body">
                <h6 className="card-title text-uppercase mb-1">Failed Logins</h6>
                <h2 className="mb-0">{stats.failedLogins}</h2>
              </div>
            </div>
          </div>
        </div>
      )}

      <div className="card shadow-sm mb-4">
        <div className="card-header">
          <h5 className="mb-0">Filters</h5>
        </div>
        <div className="card-body">
          <div className="row g-3">
            <div className="col-md-3">
              <label className="form-label">Action</label>
              <select
                className="form-select"
                value={filters.action}
                onChange={(e) => handleFilterChange('action', e.target.value)}
              >
                <option value="">All Actions</option>
                {actions.map(action => (
                  <option key={action} value={action}>{action}</option>
                ))}
              </select>
            </div>
            <div className="col-md-3">
              <label className="form-label">Actor ID</label>
              <input
                type="number"
                className="form-control"
                placeholder="Actor ID"
                value={filters.actorId}
                onChange={(e) => handleFilterChange('actorId', e.target.value)}
              />
            </div>
            <div className="col-md-3">
              <label className="form-label">Start Date</label>
              <input
                type="datetime-local"
                className="form-control"
                value={filters.startDate}
                onChange={(e) => handleFilterChange('startDate', e.target.value)}
              />
            </div>
            <div className="col-md-3">
              <label className="form-label">End Date</label>
              <input
                type="datetime-local"
                className="form-control"
                value={filters.endDate}
                onChange={(e) => handleFilterChange('endDate', e.target.value)}
              />
            </div>
            <div className="col-md-12 d-flex gap-2">
              <button
                className="btn btn-outline-secondary"
                onClick={() => setFilters({ actorId: '', action: '', startDate: '', endDate: '', sortBy: 'timestamp', sortDir: 'desc' })}
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
          ) : logs.length === 0 ? (
            <div className="text-center py-5">
              <p className="text-muted mb-0">No audit logs found</p>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Timestamp</th>
                    <th>Actor</th>
                    <th>Action</th>
                    <th>Target</th>
                    <th>Description</th>
                    <th>IP Address</th>
                  </tr>
                </thead>
                <tbody>
                  {logs.map((log) => (
                    <tr key={log.id}>
                      <td>{new Date(log.timestamp).toLocaleString()}</td>
                      <td>
                        <div>
                          <strong>{log.actorEmail}</strong>
                          <br />
                          <small className="text-muted">ID: {log.actorId}</small>
                        </div>
                      </td>
                      <td>
                        <span className={'badge ' + getActionBadgeClass(log.action)}>
                          {log.action}
                        </span>
                      </td>
                      <td>
                        {log.targetEntity && log.targetId ? (
                          <span>{log.targetEntity} #{log.targetId}</span>
                        ) : (
                          <span className="text-muted">-</span>
                        )}
                      </td>
                      <td>{log.description}</td>
                      <td><small>{log.ipAddress}</small></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
        {pagination.totalPages > 1 ? (
            <div className="card-footer bg-white border-top">
              <div>
                <nav aria-label="Audit log pagination">
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
                  Showing {pagination.page * pagination.size + 1} to {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)} of {pagination.totalElements} logs
                </p>
              </div>
            </div>
          ) : null}
        </div>
      {renderPagination()}
    </div>
  );
};

export default AuditLogs;