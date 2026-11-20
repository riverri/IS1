-- Las ligas siguen siendo privadas hasta que su creador decida publicarlas.
alter table liga add column publica boolean not null default false;
