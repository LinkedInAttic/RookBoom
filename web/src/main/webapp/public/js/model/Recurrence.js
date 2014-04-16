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

function formatRecurrence(recurrence) {
    var MILLIS_IN_MONTH = 2678400000;
    return {
        active: recurrence.active,
        firstDay: recurrence.firstDay,
        description: recurrence.description,
        params: {
            repFrom: recurrence.day,
            repPattern: recurrence.pattern,
            repInterval: recurrence.interval,
            repDays: recurrence.days ? recurrence.days.join() : "",
            repWeek: recurrence.week,
            repBy: recurrence.day + (recurrence.endAfter * MILLIS_IN_MONTH)
        }
    };
}

var Recurrence = Backbone.Model.extend({

    defaults: {
        pattern: 'weekly',
        endAfter: 1,
        interval: 1,
        week: 1,
        days: [],
        active: false
    },

    initialize: function () {
        _.bindAll(this, 'format');
    },

    format: function () {
        return formatRecurrence(this.attributes);
    }
});

var RecurrenceView = Backbone.View.extend({

    events: {
        'change .rec-pattern': 'switchPattern',
        'click .rec-apply': 'apply',
        'click .select-all-days': function () {
            $('.days .btn').addClass('active');
        },
        'click .recurrence-actions .action.reset': 'resetRecurrence',
        'click .recurrence-actions .action.collapse': 'toggleRecurrence',
        'click .recurrence-description': 'toggleRecurrence',
        'click #recurrence-toggle': 'toggleRecurrence'
    },

    initialize: function () {
        _.bindAll(this, 'render', 'switchPattern', 'apply', 'resetRecurrence', 'toggleRecurrence');

        var self = this;
        $.dustReplace('recurrence', {}, '.recurrence-view', function () {
            self.model.bind('change:pattern', self.render);
            self.setElement('#header-bar');

            $('.datepicker').datepicker();
            $('.recurrence-form').ajaxForm($.withErrorHandling());

            self.render();
        });
    },

    resetRecurrence: function () {
        this.model.set({active: false});
        this.toggleRecurrence();
    },

    toggleRecurrence: function () {
        $('.recurrence-view').slideToggle(200);
        $('#recurrence-toggle').toggleClass('active');
    },

    render: function () {
        var pattern = this.model.get('pattern');
        var model = this.model;
        var $forms = $('form.recurrence-form');
        var key = pattern.toLocaleLowerCase();

        $('.pattern-fields').hide();
        $('.pattern-fields.' + key).show();

        $forms.each(function (i, e) {
            var $form = $(e);
            var $fields = $form.find('.pattern-fields.' + key);

            $form.find('.rec-pattern').each(function () {
                $(this).find('option[value="' + pattern + '"]').selected();
            });

            $fields.find('.interval-days').each(function () {
                this.value = model.get('interval');
            });

            $fields.find('.interval-weeks').each(function () {
                this.value = model.get('interval');
            });

            $fields.find('.days').each(function () {
                _.each(model.get('days'), function (i) {
                    $fields.find('.days button[data-day="' + i + '"]').addClass('active');
                });
            });

            $form.find('.date-from').each(function () {
                var day = model.get('day');
                var from = day ? new Date(day) : new Date();
                $(this).datepicker('setDate', from);
            });

            $form.find('.end-after').each(function () {
                this.value = model.get('endAfter');
            });
        });
    },

    switchPattern: function ($event) {
        var select = $event.target;
        var key = select.options[select.selectedIndex].value;
        $('.pattern-fields').hide();
        $('.pattern-fields.' + key).show();
    },

    apply: function ($event) {
        $.gaq('recurrence', 'recurrence-schedule');

        var self = this;
        var model = this.model;
        var $target = $($event.target);
        var $form = $target.closest('form.recurrence-form');

        if ($form.find('.days .btn.active').size() === 0) {
            $.warnMsg('Please select one or more days for the recurrent meeting');
            return;
        }

        var key = $form.find('.rec-pattern').val();
        var $fields = $form.find('.pattern-fields.' + key);
        var recurrence = _.extend({}, model.defaults);

        $fields.find('.interval-days').each(function () {
            recurrence.interval = parseInt(this.value);
        });

        $fields.find('.interval-weeks').each(function () {
            recurrence.interval = parseInt(this.value);
        });

        $fields.find('.days').each(function () {
            recurrence.days = _.map($fields.find('.days').find('.active'), function (e) {
                return $(e).data('day');
            });
        });

        $form.find('.date-from').each(function () {
            var day = $(this).datepicker('getDate');
            recurrence.day = self.options.location.adjustTime(day.getTime(), true);
        });

        $form.find('.end-after').each(function () {
            recurrence.endAfter = parseInt(this.value);
        });

        recurrence.active = true;
        recurrence.pattern = key;

        $.ajax($.withErrorHandling({
            url: "/schedule/rep-info",
            data: _.extend({}, formatRecurrence(recurrence).params, {location: this.options.location.getCurrentSiteId()}),
            success: function (repInfo) {
                recurrence.firstDay = self.options.location.adjustTime(repInfo.first, false);
                recurrence.description = repInfo.description;
                model.set(recurrence);
                self.toggleRecurrence();
            },
            errorMsg: 'Unable to get recurrncy description'
        }));
    }
});