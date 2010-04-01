#
# Depends on Pygments.  To install it:
# sudo easy_install Pygments
#
module Fuse
  module PygmentizeCommon
    def pygmentize(lang, lines, body) 
      result=""
      IO.popen("pygmentize -O 'style=colorful#{lines}' -f html -l #{lang}", 'r+') do |pipe|          
        pipe.print body
        pipe.close_write
        result = pipe.read
      end
    
      if $? != 0 
        raise Exception, "'pygmentize' execution failed: #{$?}.  Did you install it from http://pygments.org/download/ ?"
      end
      result.gsub(/\r?\n/, "&#x000A;")
    end  
  end
  
  # Provides syntax highlighting via the pygmentize tool.
  class Pygmentize

    include Webgen::Tag::Base
    include Webgen::WebsiteAccess
    include Fuse::PygmentizeCommon

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
      
      result = pygmentize(lang, lines, body)
      
      # Format the result
      result = "#{tag_indent}<div class=\"syntax\">#{result}</div>\n"
      
    rescue Exception => e
      raise RuntimeError, "Error processing the pygmentize tag <#{context.ref_node.absolute_lcn}>: #{e.message}\n#{e.backtrace.join("\n")}"
    end 
  end
  
  Webgen::WebsiteAccess.website.config.fuse.pygmentize.lang('text', :doc => 'The highlighting language', :mandatory => 'default')
  Webgen::WebsiteAccess.website.config.fuse.pygmentize.lines(false, :doc => 'Should line numbers be shown')
  Webgen::WebsiteAccess.website.config['contentprocessor.tags.map']['pygmentize'] = 'Fuse::Pygmentize'

  # Provides syntax highlighting via the pygmentize tool for 2 code sections.. lays them out so they can be compared.
  class PygmentizeAndCompare

    include Webgen::Tag::Base
    include Webgen::WebsiteAccess
    include Fuse::PygmentizeCommon

    # Highlight the body of the block.
    def call(tag, body, context)
      
      # figure out the indent level of the tag
      lines = body.split(/\r?\n/);
      last_line = lines.last
      tag_indent = ""
      if( last_line.match(/^[ \t]+$/) ) 
        tag_indent = last_line
      end
      
      # Strip off the tag_indent... and figure out the sections...
      state = 0

      for i in (0 .. (lines.size-1) )
        if( tag_indent.size > 0 ) 
          lines[i] = lines[i].sub(/^[ \t]{#{tag_indent.size}}/, "")
        end
        if state==0 
          if (!lines[i].strip.empty?) 
            if( lines[i].match(/\s*------+\s*/) ) 
              state=1
            else
              raise Exception, "expecting 1st seperator: ------"
            end
          end
          
        elsif state==1 
          m = lines[i].match(/\s*([^:\s]+)\s*:\s*(.+)/)
          raise Exception, "left heading section expected." unless m
          leftlang = m[1]
          leftheading = m[2]
          leftcode =""
          state=2

        elsif state==2
          if( lines[i].match(/\s*------+\s*/) ) 
            state=3
          else
            raise Exception, "expecting 2nd seperator: ------"
          end
          
        elsif state==3
          if( lines[i].match(/\s*------+\s*/) ) 
            state=4
          else
            leftcode += lines[i]+"\n"
          end
          
        elsif state==4
          m = lines[i].match(/\s*([^:\s]+)\s*:\s*(.+)/)
          raise Exception, "right heading section expected." unless m
          rightlang = m[1]
          rightheading = m[2]
          rightcode = ""
          state=5

        elsif state==5 
          if( lines[i].match(/\s*------+\s*/) ) 
            state=6
          else
            raise Exception, "expecting 4th seperator: ------"
          end
          
        elsif state==6
          rightcode += lines[i]+"\n"
        end
      end
      
      raise Exception, "did not get all the sections: state: #{state}" unless state==6
      
      # Execute the pygmentize tool
      lines = param('fuse.pygmentize_and_compare.lines') ? ",linenos=1" : ""
      
      leftresult = pygmentize(leftlang, lines, leftcode)
      rightresult = pygmentize(rightlang, lines, rightcode)
      
      # Format the result
      result = "#{tag_indent}<div class=\"compare\"><div class=\"compare-left\"><h3>#{leftheading}</h3><div class=\"syntax\">#{leftresult}</div></div><div class=\"compare-right\"><h3>#{rightheading}</h3><div class=\"syntax\">#{rightresult}</div></div><br class=\"clear\"/></div>\n"
      
    rescue Exception => e
      raise RuntimeError, "Error processing the pygmentize_and_compare tag <#{context.ref_node.absolute_lcn}>: #{e.message}\n#{e.backtrace.join("\n")}"
    end 
  end
  
  Webgen::WebsiteAccess.website.config.fuse.pygmentize_and_compare.lines(false, :doc => 'Should line numbers be shown')
  Webgen::WebsiteAccess.website.config['contentprocessor.tags.map']['pygmentize_and_compare'] = 'Fuse::PygmentizeAndCompare'


end
