import { Header } from '@/components/layout/Header';
import { Card, CardBody } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import Link from 'next/link';
import { api } from '@/lib/api';

async function getProjects() {
  try {
    return await api.projects.list();
  } catch {
    return [];
  }
}

export default async function ProjectsPage() {
  const projects = await getProjects();

  return (
    <div>
      <Header title="Projects" />
      <div className="p-6">
        <Card>
          <CardBody>
            {projects.length === 0 ? (
              <p className="text-gray-500 text-sm">No projects found.</p>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {projects.map((p) => (
                  <Link key={p.id} href={`/projects/${p.id}`}>
                    <div className="border border-gray-200 rounded-lg p-4 hover:border-blue-300 hover:shadow-sm transition-all cursor-pointer">
                      <div className="flex items-start justify-between">
                        <div>
                          <h3 className="font-semibold text-gray-900">{p.name}</h3>
                          <p className="text-xs text-gray-500 font-mono mt-0.5">{p.key}</p>
                        </div>
                        <Badge variant={p.active ? 'success' : 'default'}>{p.active ? 'Active' : 'Inactive'}</Badge>
                      </div>
                      {p.description && <p className="text-sm text-gray-600 mt-2 line-clamp-2">{p.description}</p>}
                      {p.jiraProjectKey && (
                        <p className="text-xs text-gray-400 mt-2">Jira: {p.jiraProjectKey}</p>
                      )}
                    </div>
                  </Link>
                ))}
              </div>
            )}
          </CardBody>
        </Card>
      </div>
    </div>
  );
}
