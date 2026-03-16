import { Header } from '@/components/layout/Header';
import { StatCard } from '@/components/ui/StatCard';
import { Card, CardHeader, CardBody } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { api } from '@/lib/api';

async function getDashboardData() {
  try {
    const [staff, projects, risks, workItems] = await Promise.allSettled([
      api.staff.list(),
      api.projects.list(),
      api.risks.list(),
      api.workItems.list(),
    ]);
    return {
      staff: staff.status === 'fulfilled' ? staff.value : [],
      projects: projects.status === 'fulfilled' ? projects.value : [],
      risks: risks.status === 'fulfilled' ? risks.value : [],
      workItems: workItems.status === 'fulfilled' ? workItems.value : [],
    };
  } catch {
    return { staff: [], projects: [], risks: [], workItems: [] };
  }
}

export default async function DashboardPage() {
  const { staff, projects, risks, workItems } = await getDashboardData();

  const highRisks = risks.filter((r) => r.severity === 'HIGH');
  const activeWorkItems = workItems.filter(
    (w) => w.status && !['Done', 'Resolved', 'Closed', 'merged', 'closed'].includes(w.status)
  );

  return (
    <div>
      <Header title="Manager Dashboard" />
      <div className="p-6 space-y-6">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <StatCard label="Active Staff" value={staff.filter((s) => s.active).length} />
          <StatCard label="Active Projects" value={projects.filter((p) => p.active).length} />
          <StatCard label="Open Risk Alerts" value={risks.length} sub={`${highRisks.length} high severity`} />
          <StatCard label="Active Work Items" value={activeWorkItems.length} />
        </div>

        {risks.length > 0 && (
          <Card>
            <CardHeader>
              <h3 className="font-semibold text-gray-800">Recent Risk Alerts</h3>
            </CardHeader>
            <CardBody>
              <ul className="divide-y divide-gray-100">
                {risks.slice(0, 5).map((risk) => (
                  <li key={risk.id} className="py-3 flex items-start justify-between gap-4">
                    <div>
                      <p className="text-sm font-medium text-gray-800">{risk.title}</p>
                      {risk.detail && <p className="text-xs text-gray-500 mt-0.5">{risk.detail}</p>}
                    </div>
                    <Badge variant={risk.severity === 'HIGH' ? 'danger' : risk.severity === 'MEDIUM' ? 'warning' : 'default'}>
                      {risk.severity}
                    </Badge>
                  </li>
                ))}
              </ul>
            </CardBody>
          </Card>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Card>
            <CardHeader>
              <h3 className="font-semibold text-gray-800">Staff Overview</h3>
            </CardHeader>
            <CardBody>
              {staff.length === 0 ? (
                <p className="text-sm text-gray-500">No staff found.</p>
              ) : (
                <ul className="divide-y divide-gray-100">
                  {staff.slice(0, 6).map((s) => (
                    <li key={s.id} className="py-2 flex items-center justify-between">
                      <div>
                        <p className="text-sm font-medium text-gray-800">{s.name}</p>
                        <p className="text-xs text-gray-500">{s.team ?? 'No team'} · {s.role ?? 'No role'}</p>
                      </div>
                      <Badge variant={s.active ? 'success' : 'default'}>{s.active ? 'Active' : 'Inactive'}</Badge>
                    </li>
                  ))}
                </ul>
              )}
            </CardBody>
          </Card>

          <Card>
            <CardHeader>
              <h3 className="font-semibold text-gray-800">Projects</h3>
            </CardHeader>
            <CardBody>
              {projects.length === 0 ? (
                <p className="text-sm text-gray-500">No projects found.</p>
              ) : (
                <ul className="divide-y divide-gray-100">
                  {projects.slice(0, 6).map((p) => (
                    <li key={p.id} className="py-2 flex items-center justify-between">
                      <div>
                        <p className="text-sm font-medium text-gray-800">{p.name}</p>
                        <p className="text-xs text-gray-500">{p.key}</p>
                      </div>
                      <Badge variant={p.active ? 'info' : 'default'}>{p.active ? 'Active' : 'Inactive'}</Badge>
                    </li>
                  ))}
                </ul>
              )}
            </CardBody>
          </Card>
        </div>
      </div>
    </div>
  );
}
