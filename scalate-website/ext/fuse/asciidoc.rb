#
# Depends on hte mizuho gem.  To install it:
# gem install --source http://gems.github.com FooBarWidget-mizuho
module Fuse

  # Processes content in AsciiDoc markup using the +asciidoc+ command
  class AsciiDoc
    
    # Convert the content in +AsciiDoc+ markup to HTML.
    def call(context)
      require 'mizuho/generator'
      require 'tempfile'

      #
      # This shells out to the asciidoc python tool so we have to pass the content via
      # temp files.
      #
      ascii_doc_in = Dir::tmpdir + "/ascii_doc."+ $$.to_s() +".in"
      ascii_doc_out = Dir::tmpdir + "/ascii_doc."+ $$.to_s() +".out"
      
      File.open(ascii_doc_in, 'w') do |f|
         f.write(context.content)
      end
    
      # Generate the HTML
      Mizuho::Generator.run_asciidoc(ascii_doc_in, ascii_doc_out)
      File.unlink(ascii_doc_in);
      
      # Strip off the asciidoc layout and just get the contents.
      parser = Mizuho::Parser.new(ascii_doc_out);
      File.unlink(ascii_doc_out);
      
      context.content = "<div class=\"asciidoc\">"+parser.contents+"</div>";
      context
    rescue Exception => e
      raise RuntimeError, "Error converting AsciiDoc markup to HTML in <#{context.ref_node.absolute_lcn}>: #{e.message}\n#{e.backtrace.join("\n")}"
    end
  end
  

end
