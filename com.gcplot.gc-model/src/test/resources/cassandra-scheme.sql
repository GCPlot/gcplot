CREATE KEYSPACE gcplot
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

USE gcplot;

CREATE TABLE IF NOT EXISTS gc_analyse (
  id uuid,
  account_id varchar,
  analyse_name varchar,
  is_continuous boolean,
  start timestamp,
  last_event timestamp,
  gc_type int,
  jvm_ids set<varchar>,
  jvm_headers map<varchar, varchar>,
  jvm_md_page_size map<varchar, bigint>,
  jvm_md_phys_total map<varchar, bigint>,
  jvm_md_phys_free map<varchar, bigint>,
  jvm_md_swap_total map<varchar, bigint>,
  jvm_md_swap_free map<varchar, bigint>,
  PRIMARY KEY (account_id, id)
);