yanagishima = {
	version: '2.0',
	sitename: 'yanagishima',
	domain: '',
	contact: '',
	apis: {
		datasource: '/datasource',
		presto: '/presto',
		query: '/query',
		history: '/history',
		download: '/download',
		csvdownload: '/csvdownload',
		kill: '/kill',
		detail: '/queryDetail?queryId={queryid}&datasource={datasource}'
	},
	snippets: [{
			label: "SELECT * FROM ... LIMIT 100",
			sql: "SELECT * FROM {catalog}.{schema}.{table} LIMIT 100",
			enable: ['BASE TABLE', 'VIEW']
		},
		{
			label: "SELECT * FROM ... WHERE LATEST PARTITION LIMIT 100",
			sql: "SELECT * FROM {catalog}.{schema}.{table} WHERE DT='{yesterday}' LIMIT 100",
			enable: ['BASE TABLE', 'VIEW']
		},
		{
			label: "SHOW PRESTO VIEW DDL",
			sql: "SELECT VIEW_DEFINITION FROM {catalog}.INFORMATION_SCHEMA.VIEWS WHERE table_catalog='{catalog}' AND table_schema='{schema}' AND table_name='{table}'",
			enable: ['VIEW']
		},
		{
			label: "SHOW CREATE TABLE ...",
			sql: "SHOW CREATE TABLE {catalog}.{schema}.{table}",
			enable: ['BASE TABLE']
		},
		{
			label: "SHOW PARTITIONS FROM ...",
			sql: "SHOW PARTITIONS FROM {catalog}.{schema}.{table}",
			enable: ['BASE TABLE']
		},
		{
			label: "DESCRIBE ...",
			sql: "DESCRIBE {catalog}.{schema}.{table}",
			enable: ['BASE TABLE', 'VIEW']
		}
	]
};