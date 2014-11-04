/*
 * (c) Copyright 2014 LinkedIn Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Time utils
(function ($) {

    var defs = {
        selector: '.time',
        attribute: 'time'
    };

    function dateFromMillis(millis) {
        return new Date(parseInt(millis));
    }

    function dateFromElement(e, atr) {
        return dateFromMillis($(e).attr(atr));
    }

    function getTimeString(date) {
        var h = date.getHours();
        var ampm = h > 11 ? "pm" : "am";
        var m = date.getMinutes();
        h %= 12;
        h = h === 0 ? 12 : h;
        return m === 0
            ? h + ' ' + ampm
            : h + ':' + m + ' ' + ampm;
    }

    $.timeToString = function (time) {
        var d = new Date();
        d.setTime(time);
        return getTimeString(d);
    };

    $.timeToDateString = function (time) {
        var d = new Date();
        d.setTime(time);
        return d.getFullYear() + '-' + (d.getMonth() + 1) + '-' + d.getDate();
    };

    $.updateTime = function (options) {
        var settings = $.extend({}, defs, options);

        $(settings.selector).each(function (i, e) {
            var date = dateFromElement(e, settings.attribute);
            $(e).text(getTimeString(date));
        })
    };

    $.roundToDayDown = function (time) {
        var d = new Date(time);
        d.setHours(0);
        d.setMinutes(0);
        d.setSeconds(0);
        d.setMilliseconds(0);
        return d.getTime();
    };

})(jQuery);

// Position utils
(function ($) {
    $.positionUtils = {
        topPosition: function (e) {
            return $(e).offset().top - $(window).scrollTop();
        },

        bottomPosition: function (e) {
            return $(e).offset().top + $(e).height() - $(window).scrollTop();
        },

        leftPosition: function (e) {
            return $(e).offset().left - $(window).scrollLeft();
        }
    };

    $.handleAllClick = function (element, handlers) {
        var $element = $(element);
        $element.click(function ($event) {
            var $target = $($event.target);
            $.each(handlers, function (i, h) {
                if ($element.find(h.selector).is($target)) {
                    h.handle($event, $target);
                }
            })
        })
    };
})(jQuery);

// Dust utils
(function ($) {

    var loadTemplate = function (name, onload) {
        $.ajax({
            url: '/public/tmpl/' + name + '.dust?v=' + ROOKBOOM.buildNumber,
            dataType: 'text',
            success: function (data) {
                var compiled = dust.compile(data, name);
                dust.loadSource(compiled);
                onload(name);
            }
        })
    };

    $.dustPreload = function (templates, success) {
        var loaded = 0;
        var onload = function () {
            loaded++;
            if (loaded === templates.length) {
                $(document).ready(success);
            }
        };
        $.each(templates, function (i, e) {
            loadTemplate(e, onload);
        });
    };

    $.dustAppend = function (name, data, el, success) {
        dust.render(name, data, function (err, out) {
            $(el).append($(out));
            if (success != undefined) {
                success();
            }
        });
    };

    $.dustReplace = function (name, data, el, success) {
        dust.render(name, data, function (err, out) {
            $(el).html(out);
            if (success != undefined) {
                success();
            }
        })
    };

    $.dustRender = function (name, data) {
        var result = '';
        dust.render(name, $.extend({}, data), function (err, out) {
            result = out;
        });
        return result;
    };

    dust.filters.json = function (value) {
        return JSON.stringify(value);
    };

    dust.filters.id = function (value) {
        return value.replace(/\s/g, '-');
    };
})(jQuery);

// General utils
(function ($) {

    $.msg = function (type, msg, options) {
        var defaults = {
            text: msg,
            type: type,
            dismissQueue: true,
            layout: 'topCenter',
            template: '<div class="noty_message ' + type + '"><div class="noty_text"></div><div class="noty_close"></div></div>',
            theme: 'rookBoomTheme',
            animation: {
                open: {height: 'toggle'},
                close: {height: 'toggle'},
                easing: 'swing',
                speed: 200
            }
        };
        var opts = _.extend({}, defaults, options);
        $(function () {
            noty(opts);
        });
    };

    $.errorMsg = function (msg) {
        $.msg('error', msg);
    };

    $.warnMsg = function (msg) {
        $.msg('warning', msg);
    };

    $.successMsg = function (msg) {
        $.msg('success', msg, {timeout: 4000});
    };

    $.confirmMsg = function (msg, accept, decline) {
        var onclick = function (callback) {
            return function ($noty) {
                $noty.close();
                try {
                    callback();
                } catch (e) {
                }
            };
        };

        var buttons = [
            {
                addClass: 'btn btn-primary',
                text: 'Yes',
                onClick: onclick(accept)
            },
            {
                addClass: 'btn',
                text: 'No',
                onClick: onclick(decline)
            }
        ];

        $.msg('confirm', msg, {
            layout: 'center',
            buttons: buttons
        });
    };

    $.gaq = function (a, b, c) {
        try {
            var params = ['_trackEvent'];
            params.push(a);
            params.push(b);
            if (c) {
                params.push(c);
            }
            _gaq.push(params);
        } catch (e) {
            _gaq.push(['_trackEvent', 'error', 'error-event-generation', b]);
        }
    };

    $.withErrorHandling = function(ajaxParams) {
        return _.extend({}, ajaxParams, {
            error: function(response) {
                try {
                    // if error handler passed in the params -- call it first
                    if (_.isFunction(ajaxParams.error)) {
                        ajaxParams.error(response);
                    }
                } catch(e) {
                }

                var data = JSON.parse(response.responseText);
                // default to the generic error message
                var msg = 'Action failed due to an error';
                if (data.error) {
                    // if message from the server available -- use it
                    msg = data.error;
                } else if (ajaxParams.errorMsg) {
                    // if no message from the server, but one is provided in the params -- use it
                    msg = ajaxParams.errorMsg;
                }
                $.errorMsg(msg);
                _gaq.push(['_trackEvent', 'error', 'error-500', msg]);
            }
        });
    };

})(jQuery);