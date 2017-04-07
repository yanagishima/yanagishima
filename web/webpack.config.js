module.exports = {
    entry: [
        './source/config.js',
        './source/plugin.js',
        './source/core.js'
    ],
    output: {
        filename: "./build/index.js"
    },
    module: {
        rules: [{
                test: /\.html$/,
                use: ['raw-loader'],
            },
            {
                test: /\.(png|jpg|gif|svg)$/,
                use: ['url-loader']
            },
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            },
            {
                test: /\.scss$/,
                use: ['style-loader', 'css-loader', 'sass-loader']
            }
        ]
    },
	cache: false
//    devtool: 'source-map',
};
