# = webgen extensions directory
#
# All init.rb files anywhere under this directory get automatically loaded on a webgen run. This
# allows you to add your own extensions to webgen or to modify webgen's core!
#
# If you don't need this feature you can savely delete this file and the directory in which it is!
#
# The +config+ variable below can be used to access the Webgen::Configuration object for the current
# website.
$LOAD_PATH << File.dirname(__FILE__)
load 'fuse/asciidoc.rb'
#load 'fuse/sitecopy_rake.rb'

config = Webgen::WebsiteAccess.website.config
config['contentprocessor.map']['asciidoc'] = 'Fuse::AsciiDoc'

module Fuse 
  autoload :AsciiDoc, 'fuse/asciidoc'
  #autoload :SitecopyTask, 'fuse/sitecopy_rake'


end
