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

var DialogView = Backbone.View.extend({

    events: {
        'click a.dialog.close': function () {
            this.hide();
        },

        'keyup': function (e) {
            // close on escape
            if (e.keyCode === 27) {
                this.hide();
            }
        },

        'click body': function(e) {
            var $popover = $('.popover');
            var $element = this.current.$element;

            var outsideDialog = $popover.has(e.target).size() === 0;
            var outsideElement = !$element || $element.parent().has(e.target).size() === 0;
            var insideConfirmDialog = $('#noty_center_layout_container').has(e.target).size() > 0;

            if (this.current.type === 'popover' && outsideDialog && outsideElement && !insideConfirmDialog) {
                this.hide();
            }
        }
    },

    none: {
        show: function () {
        },
        hide: function () {
        },
        blackout: function () {
        }
    },

    popover: {
        show: function (data) {
            var html = $.dustRender(
                data.name,
                data.data
            );

            var options = $.extend({
                container: 'body'
            }, data.options);

            var $element = data.$element;
            $element.popover({
                html: true,
                content: html,
                placement: 'bottom',
                trigger: 'manual',
                delay: 0,
                animation: false,
                container: options.container
            });
            $element.popover('show');

            return {
                $element: $element
            };
        },

        hide: function () {
            $('.popover').remove();
        },

        blackout: function () {
            var $div = $('<div class="blackout"></div>');
            var $dialogPanel = $('.info-dialog');
            $div.width($dialogPanel.innerWidth());
            $div.height($dialogPanel.innerHeight());
            $dialogPanel.append($div);
        }
    },

    modal: {
        show: function (data) {
            var html = $.dustRender(
                data.name,
                data.data
            );

            //
            var $dialog = $(html);
            var $body = $('body');
            $body.append($dialog);
            $body.addClass('crop');
            $('html').addClass('crop');

            return {
                $dialog: $dialog
            }
        },

        hide: function (current) {
            $('html').removeClass('crop');
            $('body').removeClass('crop');
            if (current.$dialog) {
                current.$dialog.remove();
            }
        },

        blackout: function () {
            var $div = $('<div class="blackout"></div>');
            var $dialogPanel = $('.info-modal');
            $div.width($dialogPanel.innerWidth());
            $div.height($dialogPanel.innerHeight());
            $dialogPanel.append($div);
        }
    },

    current: {
        // type, element, key, onHide
    },

    defaults: {
        onShow: function () {
        },
        onHide: function () {
        }
    },

    initialize: function () {
        _.bindAll(this, 'renderDialog', 'hide', 'showModal', 'showPopover', 'blackout');
        this.setElement('html');
    },

    blackout: function () {
        this[this.current.type].blackout();
    },

    hide: function () {
        var onHide = this.current.onHide;
        this.popover.hide(this.current);
        this.modal.hide(this.current);
        this.current = {
            type: 'none',
            data: {}
        };
        if (onHide) {
            onHide();
        }
    },

    showModal: function (name, data, options) {
        this.renderDialog('modal', {
            name: name,
            data: data,
            options: _.extend({}, this.defaults, options)
        });
    },

    showPopover: function (name, data, element, options) {
        this.renderDialog('popover', {
            name: name,
            data: data,
            $element: $(element),
            options: _.extend({}, this.defaults, options)
        });
    },

    renderDialog: function (type, data) {
        this.hide();
        var result = this[type].show(data);
        this.current = _.extend({
            type: type,
            onHide: data.options.onHide
        }, result);
        data.options.onShow();
    }
});