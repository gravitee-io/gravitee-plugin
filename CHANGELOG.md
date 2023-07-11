# [2.0.0-alpha.5](https://github.com/gravitee-io/gravitee-plugin/compare/2.0.0-alpha.4...2.0.0-alpha.5) (2023-07-11)


### Features

* allow plugin override based on .zip file date (most recent wins) ([2660421](https://github.com/gravitee-io/gravitee-plugin/commit/266042190dc18c3a8d0e651a15c7ed360da3a06f))

# [2.0.0-alpha.4](https://github.com/gravitee-io/gravitee-plugin/compare/2.0.0-alpha.3...2.0.0-alpha.4) (2023-07-06)


### Features

* autodetect the execution phase for policies not migrated to V4 engine ([017cfbc](https://github.com/gravitee-io/gravitee-plugin/commit/017cfbcefcce10a2e1eeba8a30289419b9b6aaf1))

# [2.0.0-alpha.3](https://github.com/gravitee-io/gravitee-plugin/compare/2.0.0-alpha.2...2.0.0-alpha.3) (2023-07-03)


### Features

* load plugin based on manifest feature ([44734ef](https://github.com/gravitee-io/gravitee-plugin/commit/44734ef18100152a28f0235d71cc188fd6f52559))


### BREAKING CHANGES

* AbstractPluginHandler now relies on the plugin manifest to check the license

# [2.0.0-alpha.2](https://github.com/gravitee-io/gravitee-plugin/compare/2.0.0-alpha.1...2.0.0-alpha.2) (2023-06-27)


### Features

* allow to deploy repositories based on license ([bd39e0f](https://github.com/gravitee-io/gravitee-plugin/commit/bd39e0fea336d5f7470bb488d391d45108b57d70))

# [2.0.0-alpha.1](https://github.com/gravitee-io/gravitee-plugin/compare/1.27.0-alpha.4...2.0.0-alpha.1) (2023-06-15)


### Features

* allow plugins to be registered when not deployed ([e022030](https://github.com/gravitee-io/gravitee-plugin/commit/e0220301ab4b605c7f61502c4ec0e7c88f603d3c))


### BREAKING CHANGES

* added new deployed status on Plugin and change the plugin handler behavior
to call handle on all plugins except the one that extends AbstractSpringPluginHandler

# [1.27.0-alpha.4](https://github.com/gravitee-io/gravitee-plugin/compare/1.27.0-alpha.3...1.27.0-alpha.4) (2023-05-30)


### Bug Fixes

* bump platform repository api version ([136d4ff](https://github.com/gravitee-io/gravitee-plugin/commit/136d4ff762f8cfb0133a81d5979b77b0c6f846ef))

# [1.27.0-alpha.3](https://github.com/gravitee-io/gravitee-plugin/compare/1.27.0-alpha.2...1.27.0-alpha.3) (2023-05-29)


### Bug Fixes

* wrong classloader is used on some spring resolver ([4dcf218](https://github.com/gravitee-io/gravitee-plugin/commit/4dcf218fd4d77e71881f8bd543d8beb29222cb91))


### Features

* handle optional repository scopes ([5ff0b63](https://github.com/gravitee-io/gravitee-plugin/commit/5ff0b635abb77510cdce4a595bf02e74c33da38b))

# [1.27.0-alpha.2](https://github.com/gravitee-io/gravitee-plugin/compare/1.27.0-alpha.1...1.27.0-alpha.2) (2023-04-19)


### Features

* add new cache plugin ([1ce83e1](https://github.com/gravitee-io/gravitee-plugin/commit/1ce83e178af2dace8c090afab7c73104b02d93a3))

# [1.27.0-alpha.1](https://github.com/gravitee-io/gravitee-plugin/compare/1.26.1...1.27.0-alpha.1) (2023-04-14)


### Features

* add new plugin type for cluster ([7336c9f](https://github.com/gravitee-io/gravitee-plugin/commit/7336c9f8137df52aa1765ce91932646914605e22))

## [1.26.1](https://github.com/gravitee-io/gravitee-plugin/compare/1.26.0...1.26.1) (2023-03-02)


### Bug Fixes

* return null if file not found instead of Exception ([46bdd3d](https://github.com/gravitee-io/gravitee-plugin/commit/46bdd3db80c67ef558a67b61e0dbf1d8f8371e44))

# [1.26.0](https://github.com/gravitee-io/gravitee-plugin/compare/1.25.0...1.26.0) (2023-02-27)


### Features

* add more information properties ([75c928e](https://github.com/gravitee-io/gravitee-plugin/commit/75c928ee1c60099c40f6b7976f3250e9235b5636))

# [1.25.0](https://github.com/gravitee-io/gravitee-plugin/compare/1.24.3...1.25.0) (2022-11-28)


### Features

* add deeper file search (in subfolders) ([6b36ede](https://github.com/gravitee-io/gravitee-plugin/commit/6b36ede35d830da050430ccdca355065d2a6e8f5))

# [1.25.0-alpha.1](https://github.com/gravitee-io/gravitee-plugin/compare/1.24.1...1.25.0-alpha.1) (2022-10-26)


### Features

* add deeper file search (in subfolders) ([6b36ede](https://github.com/gravitee-io/gravitee-plugin/commit/6b36ede35d830da050430ccdca355065d2a6e8f5))

## [1.24.3](https://github.com/gravitee-io/gravitee-plugin/compare/1.24.2...1.24.3) (2022-11-24)


### Bug Fixes

* do not add plugin to PluginRegistry if not enabled ([a881c37](https://github.com/gravitee-io/gravitee-plugin/commit/a881c377a045510c9fe1e768e5a313d8503beb98))

## [1.24.2](https://github.com/gravitee-io/gravitee-plugin/compare/1.24.1...1.24.2) (2022-11-04)


### Bug Fixes

* add a type to the FetcherPluginHandler ([283d149](https://github.com/gravitee-io/gravitee-plugin/commit/283d1491097fffcf7c41208937bdf85a12480808))

## [1.24.1](https://github.com/gravitee-io/gravitee-plugin/compare/1.24.0...1.24.1) (2022-06-10)


### Bug Fixes

* fix version for all sub-modules ([8468b96](https://github.com/gravitee-io/gravitee-plugin/commit/8468b961e2c7d566f356fb7d2429179b2836dd9c))

# [1.24.0](https://github.com/gravitee-io/gravitee-plugin/compare/1.23.2...1.24.0) (2022-06-10)


### Features

* **jupiter:** detect inherited policy methods ([4560aee](https://github.com/gravitee-io/gravitee-plugin/commit/4560aeea8a588ff920cf2058e82e868bbfb7cdff))

## [1.23.2](https://github.com/gravitee-io/gravitee-plugin/compare/1.23.1...1.23.2) (2022-06-08)


### Bug Fixes

* open design of plugin instantiation ([4fc6494](https://github.com/gravitee-io/gravitee-plugin/commit/4fc64940da5d1a65f96ee08b07edc52d67c3fae2))

## [1.23.1](https://github.com/gravitee-io/gravitee-plugin/compare/1.23.0...1.23.1) (2022-05-20)


### Bug Fixes

* Add a warn for plugin which can't be deployed because of PluginDeploymentLifecycle ([f32cfc8](https://github.com/gravitee-io/gravitee-plugin/commit/f32cfc8aeadca5b26be6d11d13c5ecd82030c0d1))

# [1.23.0](https://github.com/gravitee-io/gravitee-plugin/compare/1.22.0...1.23.0) (2022-05-20)


### Features

* Single distribution bundle for CE / EE ([08dc33d](https://github.com/gravitee-io/gravitee-plugin/commit/08dc33d3ce828a3943c6f92c9a3dd89fa550be5d))
