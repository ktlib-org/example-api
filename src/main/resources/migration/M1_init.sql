CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE job (
	id varchar NOT NULL,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	last_start_time timestamptz NULL,
	name varchar not null,
	function varchar not null,
	cron varchar not null,
	enabled bool NOT NULL DEFAULT true,
	CONSTRAINT scheduled_job_pk PRIMARY KEY (id)
);

CREATE TRIGGER set_timestamp BEFORE UPDATE ON job FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TABLE performance_info (
	id varchar NOT NULL,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	time TIMESTAMPTZ NOT NULL,
	data varchar NOT NULL,
	CONSTRAINT performance_pk PRIMARY KEY (id)
);

CREATE INDEX performance_time ON performance_info USING btree (time);

CREATE TABLE "user" (
	id varchar NOT NULL,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	email varchar not null,
	password varchar not null,
	first_name varchar not null,
	last_name varchar not null,
	enabled bool NOT NULL DEFAULT true,
	employee bool NOT NULL DEFAULT false,
	password_set bool not null default false,
	locked bool not null default false,
	password_failures int2 not null default 0,
	CONSTRAINT user_email_unq UNIQUE (email),
	CONSTRAINT user_pk PRIMARY KEY (id)
);

CREATE TRIGGER set_timestamp BEFORE UPDATE ON "user" FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TABLE user_login (
	id varchar NOT NULL,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	user_id varchar not NULL,
	parent_id varchar NULL,
	token varchar not null,
	valid bool NOT NULL DEFAULT true,
	CONSTRAINT user_login_pk PRIMARY KEY (id),
	CONSTRAINT user_login_user_id_fk FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE TRIGGER set_timestamp BEFORE UPDATE ON user_login FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();

CREATE INDEX user_login_token ON user_login USING btree (token);
CREATE INDEX user_login_user_id ON user_login USING btree (user_id);

CREATE TABLE organization (
	id varchar NOT NULL,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	name varchar not null,
	CONSTRAINT organization_pk PRIMARY KEY (id)
);

CREATE TRIGGER set_timestamp BEFORE UPDATE ON organization FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TABLE organization_user (
	id varchar NOT NULL,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	user_id varchar not null,
	organization_id varchar not null,
	role varchar not null,
	CONSTRAINT organization_user_pk PRIMARY KEY (id),
	CONSTRAINT organization_user_user_id_fk FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
	CONSTRAINT organization_user_organization_id_fk FOREIGN KEY (organization_id) REFERENCES "organization"(id) ON DELETE CASCADE
);

CREATE TRIGGER set_timestamp BEFORE UPDATE ON organization_user FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();

CREATE INDEX organization_user_user_id ON organization_user USING btree (user_id);
CREATE INDEX organization_user_organization_id ON organization_user USING btree (organization_id);

CREATE TABLE user_validation (
	id varchar NOT NULL,
	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	email varchar not null,
	token varchar not null,
	first_name varchar not null,
	last_name varchar not null,
	user_id varchar NULL,
	organization_id varchar NULL,
	role varchar null,
	CONSTRAINT user_validation_pk PRIMARY KEY (id),
	CONSTRAINT user_validation_user_id_fk FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT user_validation_organization_id_fk FOREIGN KEY (organization_id) REFERENCES "organization"(id) ON DELETE CASCADE
);

CREATE TRIGGER set_timestamp BEFORE UPDATE ON user_validation FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();

CREATE INDEX user_validation_token ON user_validation USING btree (token);
CREATE INDEX user_validation_user_id ON user_validation USING btree (user_id);
CREATE INDEX user_validation_organization_id ON user_validation USING btree (organization_id);
