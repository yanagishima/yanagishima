module.exports = {
	"ui": {
		"port": 3001,
		"weinre": {
			"port": 8080
		}
	},
	"files": ["./build/index.html", "./build/share/index.html", "./build/index.js"],
	"watchEvents": [
		"change"
	],
	"watchOptions": {
		"ignoreInitial": true
	},
	"server": {
		"baseDir": "./build/",
		"middleware": function(req, res, next) {
			var timestamp = "[" + new Date().toISOString().replace(/T/, ' ').replace(/\..+/, '') + "] ";
			console.log(timestamp + req.method + " " + req.originalUrl + " - " + req.connection.remoteAddress + " - " + req.headers['user-agent']);
			next();
		}
	},
	"proxy": false,
	"port": 3000,
	"middleware": false,
	"serveStatic": [],
	"ghostMode": {
		"clicks": true,
		"scroll": true,
		"forms": {
			"submit": true,
			"inputs": true,
			"toggles": true
		}
	},
	"logLevel": "info",
	"logPrefix": "BS",
	"logConnections": false,
	"logFileChanges": true,
	"logSnippet": true,
	"rewriteRules": [],
	"open": "local",
	"browser": "default",
	"cors": false,
	"xip": false,
	"hostnameSuffix": false,
	"reloadOnRestart": false,
	"notify": true,
	"scrollProportionally": true,
	"scrollThrottle": 0,
	"scrollRestoreTechnique": "window.name",
	"scrollElements": [],
	"scrollElementMapping": [],
	"reloadDelay": 0,
	"reloadDebounce": 0,
	"reloadThrottle": 0,
	"plugins": [],
	"injectChanges": true,
	"startPath": null,
	"minify": true,
	"host": null,
	"localOnly": false,
	"codeSync": true,
	"timestamps": true,
	"clientEvents": [
		"scroll",
		"scroll:element",
		"input:text",
		"input:toggles",
		"form:submit",
		"form:reset",
		"click"
	],
	"socket": {
		"socketIoOptions": {
			"log": false
		},
		"socketIoClientConfig": {
			"reconnectionAttempts": 50
		},
		"path": "/browser-sync/socket.io",
		"clientPath": "/browser-sync",
		"namespace": "/browser-sync",
		"clients": {
			"heartbeatTimeout": 5000
		}
	},
	"tagNames": {
		"less": "link",
		"scss": "link",
		"css": "link",
		"jpg": "img",
		"jpeg": "img",
		"png": "img",
		"svg": "img",
		"gif": "img",
		"js": "script"
	}
};