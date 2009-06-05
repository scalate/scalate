# -*- ruby -*-
#
# This is a sample Rakefile to which you can add tasks to manage your website. For example, users
# may use this file for specifying an upload task for their website (copying the output to a server
# via rsync, ftp, scp, ...).
#
# It also provides some tasks out of the box, for example, rendering the website, clobbering the
# generated files, an auto render task,...
#

require 'webgen/webgentask'
require 'webgen/website'

# TODO couldn't figure out how to use the autoload stuff! :)
require 'ext/fuse/sitecopy_rake.rb'

# TODO must change this to the actual project!
# one day it would be nice to find this from the src/metainfo file
project_id = "CHANGEME"

task :default => :webgen
task :rebuild => [:clobber, :webgen]
task :upload => ["sitecopy:upload"]
task :reupload => ["sitecopy:clobber", "sitecopy:upload"]
task :deploy => ["sitecopy:clobber", :webgen, "sitecopy:upload", :linkcheck]
task :auto => :auto_webgen

Webgen::WebgenTask.new do |website|
  website.clobber_outdir = true
  website.config_block = lambda do |config|
    # you can set configuration options here
  end
end

desc "Render the website automatically on changes"
task :auto_webgen do
  puts 'Starting auto-render mode'
  time = Time.now
  abort = false
  old_paths = []
  Signal.trap('INT') {abort = true}

  while !abort
    # you may need to adjust the glob so that all your sources are included
    paths = Dir['src/**/*'].sort
    if old_paths != paths || paths.any? {|p| File.mtime(p) > time}
      Rake::Task['webgen'].execute({})
    end
    time = Time.now
    old_paths = paths
    sleep 2
  end
end


# lets parse a file from ~/.fuseforge.rc
#   username anonymous
#   password foo@bar.example
def get_username_pwd
  userpwd = ""
  userpwd_file_name = File.expand_path("~/.fuseforge.rc")
  #puts "Looking for file #{userpwd_file_name}"
  #userpwd_file_name = "/Users/jstrachan/.fuseforge.rc"
  if File.file?(userpwd_file_name) 
    userpwd = IO.readlines(userpwd_file_name).join("")
    #puts "User file is #{userpwd}"
  end
  userpwd.strip
end

# lets not use safe mode due to timestamp wierdness
#safe

Fuse::SitecopyTask.new("forgesite", <<-SITECOPYRC)
  server fusesource.com
  protocol http
  #{get_username_pwd}  
  local out
  remote /forge/dav/#{project_id}
  
  state checksum
  exclude /maven
  exclude /repo
  exclude /ignore
  exclude /ignore
  ignore /maven
  ignore /repo
  ignore /.htaccess
  exclude /.htaccess
    
SITECOPYRC

desc "checks the links on the deployed site"
task :linkcheck do
  puts 'Generating linkerrors.html file using the linkchecker executable from (http://linkchecker.sourceforge.net)'
  
  htmlcmd = "linkchecker -o html --ignore-url=^git --ignore-url=^ssh http://#{project_id}.fusesource.org/ > linkerrors.html"
  puts htmlcmd
  system htmlcmd

  puts ''
  puts 'Starting link checking'
  sh "linkchecker --ignore-url=^git --ignore-url=^ssh http://#{project_id}.fusesource.org/"
end