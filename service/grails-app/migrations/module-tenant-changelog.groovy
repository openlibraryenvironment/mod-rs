databaseChangeLog = {
  include file: 'initial-customisations.groovy'
  include file: 'initial-model.groovy'
  include file: 'update-mod-rs-2-0.groovy'
  include file: 'update-mod-rs-2-3.groovy'
  include file: 'update-mod-rs-2-4.groovy'
  include file: 'update-mod-rs-2-6.groovy'
  include file: 'update-mod-rs-2-7.groovy'
  include file: 'update-mod-rs-2-8.groovy'
  include file: 'update-mod-rs-2-9.groovy'
  include file: 'update-mod-rs-2-11.groovy'
  include file: 'update-mod-rs-2-12.groovy'
  include file: 'update-mod-rs-2-13.groovy'
  
  // Pulled in from web-toolkit-ce
  include file: 'wtk/additional_CustomPropertyDefinitions.feat.groovy'
  include file: 'wtk/multi-value-custprops.feat.groovy'
  include file: 'wtk/hidden-appsetting.feat.groovy'
}
