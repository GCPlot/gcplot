CREATE KEYSPACE IF NOT EXISTS gcplot
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 2 };

USE gcplot;

CREATE TABLE IF NOT EXISTS gc_analyse (
  id uuid,
  account_id varchar,
  analyse_name varchar,
  is_continuous boolean,
  start timestamp,
  last_event timestamp,
  jvm_ids set<varchar>,
  jvm_names map<varchar, varchar>,
  jvm_versions map<varchar, int>,
  jvm_gc_types map<varchar, int>,
  jvm_headers map<varchar, varchar>,
  jvm_md_page_size map<varchar, bigint>,
  jvm_md_phys_total map<varchar, bigint>,
  jvm_md_phys_free map<varchar, bigint>,
  jvm_md_swap_total map<varchar, bigint>,
  jvm_md_swap_free map<varchar, bigint>,
  ext varchar,
  PRIMARY KEY (account_id, id)
);

CREATE INDEX IF NOT EXISTS analyse_ids ON gc_analyse( id );

CREATE TABLE IF NOT EXISTS gc_event (
  id uuid,
  parent_id uuid,
  analyse_id uuid,
  bucket_id varchar,
  date varchar,
  jvm_id varchar,
  description varchar,
  written_at timeuuid,
  tmstm double,
  occurred timestamp,
  vm_event_type int,
  capacity list<bigint>,
  total_capacity list<bigint>,
  pause_mu bigint,
  generations bigint,
  phase int,
  concurrency int,
  ext varchar,
  PRIMARY KEY ((analyse_id, jvm_id, date), written_at)
) WITH CLUSTERING ORDER BY (written_at DESC);

CREATE INDEX IF NOT EXISTS gc_event_ids ON gc_event( id );
CREATE INDEX IF NOT EXISTS gc_event_occurred ON gc_event( occurred );

CREATE TABLE IF NOT EXISTS objects_ages (
  analyse_id uuid,
  occurred timestamp,
  written_at timeuuid,
  jvm_id varchar,
  occupied list<bigint>,
  total list<bigint>,
  ext varchar,
  PRIMARY KEY ((analyse_id, jvm_id), written_at)
) WITH CLUSTERING ORDER BY (written_at DESC);

CREATE INDEX IF NOT EXISTS objects_ages_occurred ON objects_ages( occurred );