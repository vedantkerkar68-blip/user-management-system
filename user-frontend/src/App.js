import "./App.css";
import "../node_modules/bootstrap/dist/css/bootstrap.min.css";
import Navbar from "./layout/Navbar";
import { AuthProvider, useAuth } from "./context/AuthContext";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import EmployeeList from "./pages/EmployeeList";
import EmployeeDetail from "./pages/EmployeeDetail";
import AddEmployee from "./pages/AddEmployee";
import EditEmployee from "./pages/EditEmployee";
import AuditLogs from "./pages/AuditLogs";
import Profile from "./pages/Profile";

const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, loading, hasRole } = useAuth();

  if (loading) {
    return (
      <div className="container py-5 text-center">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !hasRole(allowedRoles)) {
    return <Navigate to="/" replace />;
  }

  return children;
};

const HomeRoute = () => {
  const { user, loading, hasRole } = useAuth();

  if (loading) {
    return (
      <div className="container py-5 text-center">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (hasRole(['ADMIN', 'HR', 'MANAGER'])) {
    return <Dashboard />;
  }

  return <Navigate to="/profile" replace />;
};

const AppRoutes = () => {
  const { user, loading } = useAuth();

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
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <Login />} />
      <Route path="/register" element={user ? <Navigate to="/" replace /> : <Register />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <HomeRoute />
          </ProtectedRoute>
        }
      />
      <Route
        path="/employees"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'HR', 'MANAGER']}>
            <EmployeeList />
          </ProtectedRoute>
        }
      />
      <Route
        path="/employees/add"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'HR']}>
            <AddEmployee />
          </ProtectedRoute>
        }
      />
      <Route
        path="/employees/:id"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE']}>
            <EmployeeDetail />
          </ProtectedRoute>
        }
      />
      <Route
        path="/employees/:id/edit"
        element={
          <ProtectedRoute allowedRoles={['ADMIN', 'HR']}>
            <EditEmployee />
          </ProtectedRoute>
        }
      />
      <Route
        path="/audit-logs"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <AuditLogs />
          </ProtectedRoute>
        }
      />
      <Route
        path="/profile"
        element={
          <ProtectedRoute>
            <Profile />
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <Navbar />
        <main className="py-3">
          <AppRoutes />
        </main>
      </Router>
    </AuthProvider>
  );
}

export default App;