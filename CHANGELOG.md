## [4.0.2](https://github.com/gravitee-io/gravitee-plugin/compare/4.0.1...4.0.2) (2024-04-04)


### Bug Fixes

* **deps:** update dependency io.gravitee:gravitee-bom to v7.0.18 ([7e989bf](https://github.com/gravitee-io/gravitee-plugin/commit/7e989bf894008f5011f4d66fe743786b4eae2e35))
* **deps:** update dependency io.gravitee.cockpit:gravitee-cockpit-api to v3.0.8 ([c7186ef](https://github.com/gravitee-io/gravitee-plugin/commit/c7186ef17f12901c687c3b982cb24f02d3eb2d1b))

## [4.0.1](https://github.com/gravitee-io/gravitee-plugin/compare/4.0.0...4.0.1) (2024-04-04)


### Bug Fixes

* pom.xml to reduce vulnerabilities ([a46a521](https://github.com/gravitee-io/gravitee-plugin/commit/a46a5210b241e5474de39b5fc37fcff60ef9df7a))

# [4.0.0](https://github.com/gravitee-io/gravitee-plugin/compare/3.1.0...4.0.0) (2024-04-03)


### chore

* update cockpit api version ([181413b](https://github.com/gravitee-io/gravitee-plugin/commit/181413b5cc4774414db8e24479c27fcea0529013))


### BREAKING CHANGES

* this requires to use latest cockpit connector plugin

# [3.1.0](https://github.com/gravitee-io/gravitee-plugin/compare/3.0.0...3.1.0) (2024-01-25)


### Features

* use latest bom version ([90a6269](https://github.com/gravitee-io/gravitee-plugin/commit/90a62691744076fb3ef95a3638f0240bf493e639))

# [3.0.0](https://github.com/gravitee-io/gravitee-plugin/compare/2.2.1...3.0.0) (2024-01-16)


### Features

* introduce boot plugin concept ([20902a6](https://github.com/gravitee-io/gravitee-plugin/commit/20902a644aa822e7e5fb096f0bcf17aa839f6a4e))


### BREAKING CHANGES

* works with gravitee-node >= 6

https://gravitee.atlassian.net/browse/ARCHI-297

## [2.2.1](https://github.com/gravitee-io/gravitee-plugin/compare/2.2.0...2.2.1) (2024-01-08)


### Bug Fixes

* clone configuration instead of creating a new one ([80b3989](https://github.com/gravitee-io/gravitee-plugin/commit/80b3989161b10ad7ee6ff13d6131b7d4c76d11e9))

# [2.2.0](https://github.com/gravitee-io/gravitee-plugin/compare/2.1.2...2.2.0) (2023-11-10)


### Features

* add way to get plugin by id from plugin registry ([1cf892a](https://github.com/gravitee-io/gravitee-plugin/commit/1cf892aa266400774dc8cdc4320c5d2b91eb2341))

## [2.1.2](https://github.com/gravitee-io/gravitee-plugin/compare/2.1.1...2.1.2) (2023-11-03)


### Bug Fixes

* **annotation-processor:** prevent exception if String property ([a9d26fe](https://github.com/gravitee-io/gravitee-plugin/commit/a9d26fed9d19143a190f34c9eb68ba1674d3e76f))

## [2.1.1](https://github.com/gravitee-io/gravitee-plugin/compare/2.1.0...2.1.1) (2023-11-03)


### Bug Fixes

* allow constant in configuration bean ([d0e934b](https://github.com/gravitee-io/gravitee-plugin/commit/d0e934b02ad724fba7660e0418093b0880ccead0))

# [2.1.0](https://github.com/gravitee-io/gravitee-plugin/compare/2.0.3...2.1.0) (2023-10-31)


### Bug Fixes

* bump gravitee-common to 3.3.3 ([9b77f12](https://github.com/gravitee-io/gravitee-plugin/commit/9b77f12a0a7f63c74978fb051fe270eec0b13431))


### Features

* add new annotation and annotation processor to generate an evaluator for configuration bean ([8d0b168](https://github.com/gravitee-io/gravitee-plugin/commit/8d0b168b654a5a7c794cd3818c2364021788647b))
* order plugin so that secret-provider is always loaded first ([31d5edd](https://github.com/gravitee-io/gravitee-plugin/commit/31d5edd416918903d59574450a88a36bc663c3b8))

## [2.0.3](https://github.com/gravitee-io/gravitee-plugin/compare/2.0.2...2.0.3) (2023-09-14)


### Bug Fixes

* bump gravitee-common to 3.3.3 ([3d51cf9](https://github.com/gravitee-io/gravitee-plugin/commit/3d51cf96f086c2c039183a18dceeebce9e015fea))

## [2.0.2](https://github.com/gravitee-io/gravitee-plugin/compare/2.0.1...2.0.2) (2023-07-28)


### Bug Fixes

* avoid throwing exception during plugin loading if the introspect is failing ([f9f6838](https://github.com/gravitee-io/gravitee-plugin/commit/f9f6838d19bf6db357bc4630a05e02fabe053945))

## [2.0.1](https://github.com/gravitee-io/gravitee-plugin/compare/2.0.0...2.0.1) (2023-07-19)


### Bug Fixes

* PluginEvent ended sent before plugins list in PluginRegistryImpl ready ([e1f6297](https://github.com/gravitee-io/gravitee-plugin/commit/e1f62975fd98cc9db1441e0fa16ff42ad577818d))

# [2.0.0](https://github.com/gravitee-io/gravitee-plugin/compare/1.26.1...2.0.0) (2023-07-17)


### Bug Fixes

* bump platform repository api version ([136d4ff](https://github.com/gravitee-io/gravitee-plugin/commit/136d4ff762f8cfb0133a81d5979b77b0c6f846ef))
* remove lombok annotation failing for javadoc ([51bdd56](https://github.com/gravitee-io/gravitee-plugin/commit/51bdd562af6daff1207befe7a88ee9a28032ea37))
* wrong classloader is used on some spring resolver ([4dcf218](https://github.com/gravitee-io/gravitee-plugin/commit/4dcf218fd4d77e71881f8bd543d8beb29222cb91))


### chore

* **deps:** update gravitee-parent ([104da9a](https://github.com/gravitee-io/gravitee-plugin/commit/104da9a3c702abf1ad15c4385ad1b1ab6ae5dd00))


### Features

* add new cache plugin ([1ce83e1](https://github.com/gravitee-io/gravitee-plugin/commit/1ce83e178af2dace8c090afab7c73104b02d93a3))
* add new plugin type for cluster ([7336c9f](https://github.com/gravitee-io/gravitee-plugin/commit/7336c9f8137df52aa1765ce91932646914605e22))
* allow plugin override based on .zip file date (most recent wins) ([2660421](https://github.com/gravitee-io/gravitee-plugin/commit/266042190dc18c3a8d0e651a15c7ed360da3a06f))
* allow plugins to be registered when not deployed ([e022030](https://github.com/gravitee-io/gravitee-plugin/commit/e0220301ab4b605c7f61502c4ec0e7c88f603d3c))
* allow to deploy repositories based on license ([bd39e0f](https://github.com/gravitee-io/gravitee-plugin/commit/bd39e0fea336d5f7470bb488d391d45108b57d70))
* autodetect the execution phase for policies not migrated to V4 engine ([017cfbc](https://github.com/gravitee-io/gravitee-plugin/commit/017cfbcefcce10a2e1eeba8a30289419b9b6aaf1))
* handle optional repository scopes ([5ff0b63](https://github.com/gravitee-io/gravitee-plugin/commit/5ff0b635abb77510cdce4a595bf02e74c33da38b))
* load plugin based on manifest feature ([44734ef](https://github.com/gravitee-io/gravitee-plugin/commit/44734ef18100152a28f0235d71cc188fd6f52559))


### BREAKING CHANGES

* **deps:** require Java17
* AbstractPluginHandler now relies on the plugin manifest to check the license
* added new deployed status on Plugin and change the plugin handler behavior
to call handle on all plugins except the one that extends AbstractSpringPluginHandler

# [2.0.0-alpha.6](https://github.com/gravitee-io/gravitee-plugin/compare/2.0.0-alpha.5...2.0.0-alpha.6) (2023-07-11)


### Bug Fixes

* remove lombok annotation failing for javadoc ([51bdd56](https://github.com/gravitee-io/gravitee-plugin/commit/51bdd562af6daff1207befe7a88ee9a28032ea37))

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
