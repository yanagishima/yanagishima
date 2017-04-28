yanagishima = {
	version: '3.0',
	sitename: 'yanagishima',
	domain: '',
	apis: {
		datasource: '/datasource',
		presto: '/presto?datasource={datasource}',
		prestoAsync: '/prestoAsync?datasource={datasource}',
		queryStatus: '/queryStatus?datasource={datasource}&queryid={queryid}',
		query: '/query?datasource={datasource}',
		history: '/history?datasource={datasource}&queryid={queryid}',
		historyStatus: '/historyStatus?datasource={datasource}&queryid={queryid}',
		queryHistory: '/queryHistory?datasource={datasource}&queryids={queryids}',
		download: '/download?datasource={datasource}&queryid={queryid}',
		csvdownload: '/csvdownload?datasource={datasource}&queryid={queryid}',
		publish: '/publish?datasource={datasource}&queryid={queryid}',
		bookmark: '/bookmark?datasource={datasource}',
		format: '/format',
		kill: '/kill?datasource={datasource}&queryid={queryid}',
		detail: '/queryDetail?datasource={datasource}&queryid={queryid}',
		shareHistory: '/share/shareHistory?publish_id={publish_id}',
		shareDownload: '/share/download?publish_id={publish_id}',
		shareCsvDownload: '/share/csvdownload?publish_id={publish_id}'
	},
	links: {
		about_this: 'https://github.com/wyukawa/yanagishima/blob/master/README.md',
		bugs_feedback: 'https://github.com/wyukawa/yanagishima/issues',
		mail_admin: '',
	},
	columnDate_names: [
		'dt',
		'yyyymmdd',
		'log_date'
	],
	default_catalog: '',
	snippets: [{
			label: "SELECT * FROM ... LIMIT 100",
			sql: "SELECT {columns} FROM {catalog}.{schema}.{table} LIMIT 100",
			enable: ['BASE TABLE', 'VIEW']
		},
		{
			label: "SELECT * FROM ... WHERE ${column_date}=${yesterday} LIMIT 100",
			sql: "SELECT {columns} FROM {catalog}.{schema}.{table} WHERE {column_date}='{yesterday}' LIMIT 100",
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
	],
	themes: [
		'ambiance',
		'chaos',
		'chrome',
		'clouds',
		'clouds_midnight',
		'cobalt',
		'crimson_editor',
		'dawn',
		'eclipse',
		'github',
		'idle_fingers',
		'iplastic',
		'katzenmilch',
		'kuroir',
		'kr_theme',
		'merbivore',
		'merbivore_soft',
		'mono_industrial',
		'monokai',
		'pastel_on_dark',
		'solarized_dark',
		'solarized_light',
		'sqlserver',
		'terminal',
		'textmate',
		'tomorrow',
		'tomorrow_night',
		'tomorrow_night_blue',
		'tomorrow_night_bright',
		'tomorrow_night_eighties',
		'twilight',
		'vibrant_ink',
		'xcode'
	]
};