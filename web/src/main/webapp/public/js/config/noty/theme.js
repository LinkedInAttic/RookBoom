;(function($) {

  $.noty.themes.defaultTheme = {
    name: 'rookBoomTheme',
    helpers: {
      borderFix: function() {
        if (this.options.dismissQueue) {
          var selector = this.options.layout.container.selector + ' ' + this.options.layout.parent.selector;
          switch (this.options.layout.name) {
            case 'top':
              $(selector).css({borderRadius: '0px 0px 0px 0px'});
              $(selector).last().css({borderRadius: '0px 0px 1px 1px'}); break;
            case 'topCenter': case 'topLeft': case 'topRight':
            case 'bottomCenter': case 'bottomLeft': case 'bottomRight':
            case 'center': case 'centerLeft': case 'centerRight': case 'inline':
            $(selector).css({borderRadius: '0px 0px 0px 0px'});
            $(selector).first().css({'border-top-left-radius': '1px', 'border-top-right-radius': '1px'});
            $(selector).last().css({'border-bottom-left-radius': '1px', 'border-bottom-right-radius': '1px'}); break;
            case 'bottom':
              $(selector).css({borderRadius: '0px 0px 0px 0px'});
              $(selector).first().css({borderRadius: '1px 1px 0px 0px'}); break;
            default: break;
          }
        }
      }
    },
    modal: {
      css: {
        position: 'fixed',
        width: '100%',
        height: '100%',
        backgroundColor: '#000',
        zIndex: 10000,
        opacity: 0.6,
        display: 'none',
        left: 0
      }
    },
    style: function() {

      this.$bar.css({
        overflow: 'hidden'
      });

      this.$message.css({
        fontSize: '13px',
        lineHeight: '16px',
        textAlign: 'center',
        //padding: '8px 10px 9px 40px'
        width: 'auto',
        position: 'relative'
      });

      this.$closeButton.css({
        position: 'absolute',
        top: 4, right: 4,
        width: 10, height: 10,
        display: 'none',
        cursor: 'pointer'
      });

      this.$buttons.css({
        padding: 5,
        textAlign: 'right'
      });

      this.$buttons.find('button').css({
        marginLeft: 5
      });

      this.$buttons.find('button:first').css({
        marginLeft: 0
      });

      this.$bar.bind({
        mouseenter: function() { $(this).find('.noty_close').fadeIn(); },
        mouseleave: function() { $(this).find('.noty_close').fadeOut(); }
      });

      switch (this.options.layout.name) {
        case 'top':
          this.$bar.css({
            borderRadius: '0px 0px 1px 1px',
            borderBottom: '1px solid #eee',
            borderLeft: '1px solid #eee',
            borderRight: '1px solid #eee',
            boxShadow: '0 1px 4px rgba(0, 0, 0, 0.1)'
          });
          break;
        case 'topCenter': case 'center': case 'bottomCenter': case 'inline':
        this.$bar.css({
          borderRadius: '1px',
          border: '1px solid #eee',
          boxShadow: '0 1px 4px rgba(0, 0, 0, 0.1)'
        });
        this.$message.css({fontSize: '13px', textAlign: 'center'});
        break;
        case 'topLeft': case 'topRight':
        case 'bottomLeft': case 'bottomRight':
        case 'centerLeft': case 'centerRight':
        this.$bar.css({
          borderRadius: '1px',
          border: '1px solid #eee',
          boxShadow: '0 1px 4px rgba(0, 0, 0, 0.1)'
        });
        this.$message.css({fontSize: '13px', textAlign: 'left'});
        break;
        case 'bottom':
          this.$bar.css({
            borderRadius: '1px 1px 0px 0px',
            borderTop: '1px solid #eee',
            borderLeft: '1px solid #eee',
            borderRight: '1px solid #eee',
            boxShadow: '0 -1px 4px rgba(0, 0, 0, 0.1)'
          });
          break;
        default:
          this.$bar.css({
            border: '1px solid #eee',
            boxShadow: '0 1px 4px rgba(0, 0, 0, 0.1)'
          });
          break;
      }

      switch (this.options.type) {
        case 'error':
          this.$bar.css({backgroundColor: 'red', borderColor: 'darkred', color: '#FFF'});
          this.$message.css({fontWeight: 'bold', padding: '8px 10px 9px 40px'});
          break;
        case 'success':
          this.$bar.css({backgroundColor: 'lightgreen', borderColor: '#50C24E', color: '#FFF'});
          this.$message.css({fontWeight: 'bold', padding: '8px 10px 9px 40px'});
          break;
        case 'warning':
          this.$bar.css({backgroundColor: '#FFEAA8', borderColor: '#FFC237', color: '#FFF'});
          this.$message.css({fontWeight: 'bold'});
          break;
        case 'confirm':
          this.$bar.css({backgroundColor: 'gray', borderColor: '#48413D', borderWidth: '2px', color: '#333'});
          this.$message.css({fontWeight: 'bold', padding: '6px 5px 6px 5px'});
          break;

        case 'information':
          this.$bar.css({backgroundColor: '#57B7E2', borderColor: '#0B90C4', color: '#FFF'});
          break;
        case 'alert':
        case 'notification':
          this.$bar.css({backgroundColor: '#FFF', borderColor: '#CCC', color: '#444'});
          break;
        default:
          this.$bar.css({backgroundColor: '#FFF', borderColor: '#CCC', color: '#444'}); break;
      }
    },
    callback: {
      onShow: function() { $.noty.themes.defaultTheme.helpers.borderFix.apply(this); },
      onClose: function() { $.noty.themes.defaultTheme.helpers.borderFix.apply(this); }
    }
  };

})(jQuery);

(function($) {

  $.noty.layouts.topCenter = {
    name: 'topCenter',
    options: { // overrides options

    },
    container: {
      object: '<ul id="noty_topCenter_layout_container" />',
      selector: 'ul#noty_topCenter_layout_container',
      style: function() {
        $(this).css({
          top: 75,
          left: 0,
          position: 'fixed',
          width: '310px',
          height: 'auto',
          margin: 0,
          listStyleType: 'none',
          zIndex: 10000000
        });

        $(this).css({
          left: ($(window).width() - $(this).outerWidth(false)) / 2 + 'px'
        });
      }
    },
    parent: {
      object: '<li />',
      selector: 'li',
      css: {}
    },
    css: {
      display: 'none',
      width: '310px'
    },
    addClass: ''
  };

})(jQuery);

(function($) {

  $.noty.layouts.center = {
    name: 'center',
    options: { // overrides options

    },
    container: {
      object: '<ul id="noty_center_layout_container" />',
      selector: 'ul#noty_center_layout_container',
      style: function() {
        $(this).css({
          position: 'fixed',
          minWidth: '200px',
          height: 'auto',
          margin: 0,
          padding: 0,
          listStyleType: 'none',
          zIndex: 10000000
        });

        // getting hidden height
        var dupe = $(this).clone().css({visibility:"hidden", display:"block", position:"absolute", top: 0, left: 0}).attr('id', 'dupe');
        $("body").append(dupe);
        dupe.find('.i-am-closing-now').remove();
        dupe.find('li').css('display', 'block');
        var actual_height = dupe.height();
        dupe.remove();

        if ($(this).hasClass('i-am-new')) {
          $(this).css({
            left: ($(window).width() - $(this).outerWidth(false)) / 2 + 'px',
            top: ($(window).height() - actual_height) / 2 + 'px'
          });
        } else {
          $(this).animate({
            left: ($(window).width() - $(this).outerWidth(false)) / 2 + 'px',
            top: ($(window).height() - actual_height) / 2 + 'px'
          }, 500);
        }

      }
    },
    parent: {
      object: '<li />',
      selector: 'li',
      css: {}
    },
    css: {
      display: 'none',
      minWidth: '200px'
    },
    addClass: ''
  };

})(jQuery);
