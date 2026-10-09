import { useQuery } from '@tanstack/react-query';
import api from '../api/axios';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';

interface RegistrationData {
  registrationDate: string;
  count: number;
}

export default function Analytics() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['analytics'],
    queryFn: async () => {
      const res = await api.get('/api/analytics/patients/summary');
      return res.data as RegistrationData[];
    }
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="heading-lg text-ink">Analytics Dashboard</h1>
        <p className="text-ink-mute mt-1">Patient registration metrics over time.</p>
      </div>

      <div className="card-light h-96 flex flex-col">
        <h2 className="heading-sm mb-6 text-ink">Daily Registrations</h2>
        
        {isLoading ? (
          <div className="flex-1 flex items-center justify-center text-ink-mute">Loading data...</div>
        ) : isError ? (
          <div className="flex-1 flex items-center justify-center text-accent-ruby">Failed to load analytics data</div>
        ) : (
          <div className="flex-1 w-full min-h-0">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={data || []} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e3e8ee" />
                <XAxis 
                  dataKey="registrationDate" 
                  axisLine={false} 
                  tickLine={false} 
                  tick={{ fill: '#64748d', fontSize: 13 }}
                  dy={10}
                />
                <YAxis 
                  axisLine={false} 
                  tickLine={false} 
                  tick={{ fill: '#64748d', fontSize: 13 }}
                />
                <Tooltip 
                  cursor={{ fill: '#f6f9fc' }}
                  contentStyle={{ borderRadius: '8px', border: '1px solid #e3e8ee', boxShadow: 'rgba(0, 55, 112, 0.08) 0 8px 24px' }}
                />
                <Bar 
                  dataKey="count" 
                  fill="#533afd" 
                  radius={[4, 4, 0, 0]}
                  barSize={40}
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>
      
      {/* Warm interlude card as per DESIGN.md */}
      <div className="card-cream mt-8">
        <h3 className="heading-md">Looking for more insights?</h3>
        <p className="text-ink-secondary mt-2 text-sm">
          Export full registration history and billing analytics from the reporting module.
        </p>
        <button className="btn-primary mt-4 text-sm px-4 py-2">View Reports</button>
      </div>
    </div>
  );
}
