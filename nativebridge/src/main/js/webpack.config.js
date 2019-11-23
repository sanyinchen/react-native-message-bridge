/**
 * Created by sanyinchen on 19-11-22.
 *
 * @author sanyinchen
 * @version v0.1
 * @since 19-11-22
 */
const path = require('path');

module.exports = {
    entry: './BatchedBridge/NativeModules.js',
    output: {
        filename: 'js-bridge-bundle.js',
        path: path.resolve(__dirname, '../assets')
    },
    module: {
        rules: [
            {
                test: /\.m?js$/,
                exclude: /(node_modules|bower_components)/,
                use: {
                    loader: 'babel-loader',
                    options: {
                        presets: ['@babel/preset-env', '@babel/preset-flow']
                    }
                }
            }
        ]
    }
};