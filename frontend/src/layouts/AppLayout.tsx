import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { Users, BarChart2, LogOut } from 'lucide-react';

export default function AppLayout() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-canvas-soft flex">
      {/* Sidebar */}
      <aside className="w-64 bg-brand-dark-900 text-on-primary flex flex-col">
        <div className="p-6">
          <h2 className="text-xl font-light tracking-tight">Patient Management</h2>
        </div>
        
        <nav className="flex-1 px-4 space-y-2 mt-4">
          <NavLink 
            to="/patients" 
            className={({ isActive }) => 
              `flex items-center gap-3 px-4 py-2 rounded-md transition-colors ${isActive ? 'bg-primary-press text-white' : 'text-on-primary/70 hover:bg-white/10 hover:text-white'}`
            }
          >
            <Users size={18} />
            <span className="font-medium text-sm">Patients</span>
          </NavLink>
          
          <NavLink 
            to="/analytics" 
            className={({ isActive }) => 
              `flex items-center gap-3 px-4 py-2 rounded-md transition-colors ${isActive ? 'bg-primary-press text-white' : 'text-on-primary/70 hover:bg-white/10 hover:text-white'}`
            }
          >
            <BarChart2 size={18} />
            <span className="font-medium text-sm">Analytics</span>
          </NavLink>
        </nav>

        <div className="p-4 border-t border-white/10">
          <button 
            onClick={handleLogout}
            className="flex items-center gap-3 w-full px-4 py-2 rounded-md text-on-primary/70 hover:bg-white/10 hover:text-white transition-colors"
          >
            <LogOut size={18} />
            <span className="font-medium text-sm">Sign Out</span>
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-auto p-8">
        <Outlet />
      </main>
    </div>
  );
}
