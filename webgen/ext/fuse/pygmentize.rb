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
      require 'tempfile'

      #
      # This shells out to the pygmentize python tool so we have to pass the content via
      # temp files.
      #
      pygmentize_in = Dir::tmpdir + "/pygmentize."+ $$.to_s() +".in"
      pygmentize_out = Dir::tmpdir + "/pygmentize."+ $$.to_s() +".out"
      
      # figure out the indent level..
      indent_level = -1;
      indent_text = ""
      for i in body.split(/\r?\n/)
        rc = i.match("^([ \t]*)[^ \t\r\n]+")  
        if( rc )  
          if( indent_level == -1 )
            indent_level = rc[1].size
            indent_text = rc[1]
          else
            indent_level = rc[1].size if rc[1].size < indent_level
            indent_text = rc[1]
          end
        end
      end
      
      # Strip off the indent...
      if( indent_level > 0 ) 
        buffer = ""
        for i in body.split(/\r?\n/)
          # v = i.sub(/^[ \t]{#{indent_level}}/, "")
          # puts "[#{i}][#{v}]"
          buffer += i.sub(/^[ \t]{#{indent_level}}/, "")+"\n"
        end
        body = buffer.chomp
      end
      
      # Write the file.
      body.split
      File.open(pygmentize_in, 'w') do |f|
         f.write(body)
      end

      # Execute the pygmentize tool
      lang = param('fuse.pygmentize.lang')
      lines = param('fuse.pygmentize.lines') ? ",linenos=1" : ""
      `pygmentize -O 'style=colorful#{lines}' -f html -l #{lang} -o #{pygmentize_out} #{pygmentize_in}`
      File.unlink(pygmentize_in);
      
      # Load the results back in...
      result = ""
      File.open(pygmentize_out, 'r') do |f|
        result = f.readlines(nil)[0]
      end
      File.unlink(pygmentize_out);
      
      # Apply white space preservation..
      result = result.gsub(/\r?\n/, "&#x000A;")
      
      # Format the result
      result = "#{indent_text}<div class=\"syntax\">#{result}</div>\n"
      
      # restorr the indent level
      result
      
    rescue Exception => e
      raise RuntimeError, "Error processing the pygmentize tag <#{context.ref_node.absolute_lcn}>: #{e.message}\n#{e.backtrace.join("\n")}"
    end 
  end
  
  Webgen::WebsiteAccess.website.config.fuse.pygmentize.lang('text', :doc => 'The highlighting language', :mandatory => 'default')
  Webgen::WebsiteAccess.website.config.fuse.pygmentize.lines(false, :doc => 'Should line numbers be shown')
  Webgen::WebsiteAccess.website.config['contentprocessor.tags.map']['pygmentize'] = 'Fuse::Pygmentize'

end
