#
# Depends on Pygments.  To install it:
# sudo easy_install Pygments
#
module Fuse

  # Provides syntax highlighting via the pygmentize tool.
  class Pygmentize

    include Webgen::Tag::Base
    include Webgen::WebsiteAccess

    # Highlight the body of the block.
    def call(tag, body, context)
      
      # figure out the indent level of the tag
      last_line = body.split(/\r?\n/).last
      tag_indent = ""
      if( last_line.match(/^[ \t]+$/) ) 
        tag_indent = last_line
      end
      
      # Strip off the tag_indent...
      if( tag_indent.size > 0 ) 
        buffer = ""
        for i in body.split(/\r?\n/)
          buffer += i.sub(/^[ \t]{#{tag_indent.size}}/, "")+"\n"
        end
        body = buffer.chomp
      end
      
      # Execute the pygmentize tool
      lang = param('fuse.pygmentize.lang')
      lines = param('fuse.pygmentize.lines') ? ",linenos=1" : ""
      
      result=""
      IO.popen("pygmentize -O 'style=colorful#{lines}' -f html -l #{lang}", 'r+') do |pipe|          
        pipe.print body
        pipe.close_write
        result = pipe.read
      end
      
      if $? != 0 
        raise Exception, "'pygmentize' execution failed: #{$?}.  Did you install it from http://pygments.org/download/ ?"
      end
      
      # Apply white space preservation..
      result = result.gsub(/\r?\n/, "&#x000A;")
      
      # Format the result
      result = "#{tag_indent}<div class=\"syntax\">#{result}</div>\n"
      
    rescue Exception => e
      raise RuntimeError, "Error processing the pygmentize tag <#{context.ref_node.absolute_lcn}>: #{e.message}\n#{e.backtrace.join("\n")}"
    end 
  end
  
  Webgen::WebsiteAccess.website.config.fuse.pygmentize.lang('text', :doc => 'The highlighting language', :mandatory => 'default')
  Webgen::WebsiteAccess.website.config.fuse.pygmentize.lines(false, :doc => 'Should line numbers be shown')
  Webgen::WebsiteAccess.website.config['contentprocessor.tags.map']['pygmentize'] = 'Fuse::Pygmentize'

end
