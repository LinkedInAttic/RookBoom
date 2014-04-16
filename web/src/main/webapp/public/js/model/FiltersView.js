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

var FiltersView = Backbone.View.extend({

    events: {
        'click #date-view .cal': 'showDatePicker',
        'click #date-view .prev': function () {
            $.gaq('calendar', 'calendar-previous');
            this.shiftDay(-1);
        },
        'click #date-view .next': function () {
            $.gaq('calendar', 'calendar-next');
            this.shiftDay(1);
        },
        'keyup #name-filter': 'nameFilter',
        'click .filter-value': 'updateFilter',
        'click .filter-reset': 'onResetFilter',
        'click .filters-actions .action.reset': 'onResetAllFilters',
        'click .filters-actions .action.collapse': 'toggleFilters',
        'click #filter-toggle': 'toggleFilters',
        'click .timeframe-value': 'onTimeframeChange'
    },

    initialize: function () {
        _.bindAll(
            this,
            'onDateChange',
            'showDatePicker',
            'onSizeChange',
            'shiftDay',
            'render',
            'updateFilter',
            'resetFilter',
            'toggleFilters',
            'onResetFilter',
            'onResetAllFilters',
            'onTimeframeChange'
        );
        this.setElement('#header-bar');
        $(window).scroll(this.onSizeChange);
        $(window).resize(this.onSizeChange);

        var self = this;
        $('#menu-about').click(function () {
            self.options.dialog.showModal('about', {email: ROOKBOOM.contactsEmail});
            $.gaq('info', 'info-about');
        });

        this.render();
        this.model.bind('change', this.render);
        this.options.rooms.bind('change:scheduleOriginal', this.render);
    },

    nameFilter: function () {
        var val = $.trim($('#name-filter').val());
        if (val.length === 0) {
            this.model.unset('name');
        } else {
            this.model.set('name', val);
        }
    },

    showDatePicker: function () {
        $.gaq('calendar', 'calendar-date');
        var self = this;
        this.options.dialog.showPopover(
            'date_picker',
            {},
            '#date-view',
            {
                onShow: function () {
                    var $datepicker = $('#datepicker').datepicker({
                        onSelect: function () {
                            self.onDateChange($datepicker);
                            self.options.dialog.hide();
                        }
                    });
                    $datepicker.datepicker('setDate', new Date(self.model.get('day')));
                },
                container: '#board-header'
            }
        );
    },

    shiftDay: function (shift) {
        var day = 1000 * 60 * 60 * 24;
        // those bells and whistles are required to take daylight savings into account
        // i.e. 'after' may have different timezoneOffset than 'before'
        var before = new Date(this.model.get('day'));
        var after = new Date(this.model.get('day') + shift * day);
        var timeZoneAdjustment = (after.getTimezoneOffset() - before.getTimezoneOffset()) * 60 * 1000;
        this.model.set('day', after.getTime() + timeZoneAdjustment);
    },

    onDateChange: function ($datepicker) {
        var day = $datepicker.datepicker('getDate');
        this.model.set('day', day.getTime());
    },

    onSizeChange: function () {
        var leftOffset = $.positionUtils.leftPosition('#board');
        if (leftOffset >= 0) {
            $('#board-header').css('left', '');
        } else {
            $('#board-header').css('left', leftOffset + 'px');
        }
    },

    render: function () {
        var filters = _.reject(this.options.manager.presentation(), function (filter) {
            return filter.kind != 'binary' && filter.values.length < 2;
        });
        var timeframe = this.model.get('timeframe');
        $.dustReplace('filters', {
            filters: filters,
            timeframe: [
                {name: 'Morning', value: 'Morning', active: timeframe === 'Morning'},
                {name: 'Day', value: 'Day', active: timeframe === 'Day'},
                {name: 'Night', value: 'Night', active: timeframe === 'Night'}
            ]
        }, '.config-view');

        this.options.recurrenceView.render();
    },

    onTimeframeChange: function (e) {
        var $target = $(e.target),
            $list = $target.parent(),
            timeframe = $target.data('value');
        $list.find('li').removeClass('active');
        $list.find('li[data-value="' + timeframe + '"]').addClass('active');
        this.model.set('timeframe', timeframe);
    },

    toggleFilters: function () {
        $('.config-view').slideToggle(200);
        $('#filter-toggle').toggleClass('active');
    },

    updateFilter: function ($event) {
        var $target = $($event.target);
        var $reset = $target.parent().find('.filter-reset');
        var field = $target.attr('field');
        var value = $target.attr('val');

        $target.toggleClass('active');
        this.model.toggleValue(field, value);

        var showReset = $target.parent().find('.filter-value.active').size() > 0;
        if (showReset) {
            $reset.removeClass('active');
        } else {
            $reset.addClass('active');
        }
        $.gaq('filter', 'filter-updated', field);
    },

    onResetFilter: function ($event) {
        this.resetFilter($($event.target));
    },

    onResetAllFilters: function () {
        var self = this;
        $('.filter-reset').each(function () {
            self.resetFilter($(this));
        });
        $.gaq('filter', 'filter-reset-all');
    },

    resetFilter: function ($target) {
        var $values = $target.parent().find('.filter-value');
        var filterValues = this.model;

        $values.removeClass('active');
        $target.addClass('active');

        _.each(_.uniq(_.map($values, function (val) {
            return $(val).attr('field');
        })), function (field) {
            filterValues.resetFilter(field);
        });
        $.gaq('filter', 'filter-reset');
    }
});