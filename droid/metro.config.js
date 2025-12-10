const { getDefaultConfig, mergeConfig } = require('@react-native/metro-config');

/**
 * Metro configuration
 * https://reactnative.dev/docs/metro
 *
 * @type {import('@react-native/metro-config').MetroConfig}
 */
const config = {
  resolver: {
    assetExts: ['png', 'jpg', 'jpeg', 'gif', 'svg', 'webp'],
  },
  server: {
    port: 8082,
  },
};

module.exports = mergeConfig(getDefaultConfig(__dirname), config);
