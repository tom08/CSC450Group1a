
/*!
 * @preserve
 * riveted.js | v0.6.0
 * Copyright (c) 2015 Rob Flaherty (@robflaherty)
 * Licensed under the MIT license
 * Modified 11-30-2016
 * by Dillon Woollums (Woollums1806@live.missouristate.edu)
 */

var riveted = (function() {

    var  started = false;
    var  stopped = false;
    var  turnedOff = false;
    var  clockTime = 0;
    var  startTime = new Date();
    var  clockTimer = null;
    var  idleTimer = null;
    var  visitTime = 0;
    var  hiddenTimer;
    var  hiddenTime = 0;
	var  sendEvent;
    var  sendUserTiming;
    var  reportInterval;
    var  idleTimeout;
    var  nonInteraction;
    var  universalGA;
    var  classicGA;
    var  universalSendCommand;
    var  googleTagManager;
    var  rivetedClass;
    var  gaGlobal;

    function init(userRivetedClass, options) {

      // Set class to apply riveted to
      rivetedClass = userRivetedClass || '';
      var element = $('#' + rivetedClass);

      // Set up options and defaults
      options = options || {};
      reportInterval = parseInt(options.reportInterval, 10) || 5;
      idleTimeout = parseInt(options.idleTimeout, 10) || 30;
      gaGlobal = options.gaGlobal || 'ga';
      totalTimeEvent();
      addEvent();

      /*
       * Determine which version of GA is being used
       * "ga", "_gaq", and "dataLayer" are the possible globals
       */

      if (typeof window[gaGlobal] === "function") {
        universalGA = true;
      }

      if (typeof _gaq !== "undefined" && typeof _gaq.push === "function") {
        classicGA = true;
      }

      if (typeof dataLayer !== "undefined" && typeof dataLayer.push === "function") {
        googleTagManager = true;
      }

      if ('gaTracker' in options && typeof options.gaTracker === 'string') {
        universalSendCommand = options.gaTracker + '.send';
      } else {
        universalSendCommand = 'send';
      }

      if (typeof options.eventHandler == 'function') {
        sendEvent = options.eventHandler;
      }

      if (typeof options.userTimingHandler == 'function') {
        sendUserTiming = options.userTimingHandler;
      }

      if ('nonInteraction' in options && (options.nonInteraction === false || options.nonInteraction === 'false')) {
        nonInteraction = false;
      } else {
        nonInteraction = true;
      }

      // Basic activity event listeners
      // addListener(element, 'keydown', trigger);
      // addListener(element, 'click', trigger);
      // addListener(element, 'mousemove', throttle(trigger, 500));
      // addListener(element, 'scroll', throttle(trigger, 500));

      element.on('keydown click mousemove scroll', trigger);

      // Page visibility listeners
      addListener(document, 'visibilitychange', visibilityChange);
      addListener(document, 'webkitvisibilitychange', visibilityChange);
    }


    /*
     * Throttle function borrowed from:
     * Underscore.js 1.5.2
     * http://underscorejs.org
     * (c) 2009-2013 Jeremy Ashkenas, DocumentCloud and Investigative Reporters & Editors
     * Underscore may be freely distributed under the MIT license.
     */

    function throttle(func, wait) {
      var context, args, result;
      var timeout = null;
      var previous = 0;
      var later = function() {
        previous = new Date;
        timeout = null;
        result = func.apply(context, args);
      };
      return function() {
        var now = new Date;
        if (!previous) previous = now;
        var remaining = wait - (now - previous);
        context = this;
        args = arguments;
        if (remaining <= 0) {
          clearTimeout(timeout);
          timeout = null;
          previous = now;
          result = func.apply(context, args);
        } else if (!timeout) {
          timeout = setTimeout(later, remaining);
        }
        return result;
      };
    }

    /*
     * Cross-browser event listening
     */

    function addListener(element, eventName, handler) {
      if (element.addEventListener) {
        element.addEventListener(eventName, handler, false);
      }
      else if (element.attachEvent) {
        element.attachEvent('on' + eventName, handler);
      }
      else {
        element['on' + eventName] = handler;
      }
    }

    /*
     * Function for logging User Timing event on initial interaction
     */

    sendUserTiming = function (timingValue) {

      if (googleTagManager) {

        dataLayer.push({'event':'RivetedTiming', 'eventCategory':'Riveted', 'timingVar': 'First Interaction', 'timingValue': timingValue});

      } else {

        if (universalGA) {
          window[gaGlobal](universalSendCommand, 'timing', 'Riveted', 'First Interaction', timingValue);
        }

        if (classicGA) {
          _gaq.push(['_trackTiming', 'Riveted', 'First Interaction', timingValue, null, 100]);
        }

      }

    };

    /*
     * Function for logging ping events
     */

    sendEvent = function (time) {

      if (googleTagManager) {

        dataLayer.push({'event':'Riveted', 'eventCategory':'Riveted', 'eventAction': 'poop', 'eventLabel': time, 'eventValue': reportInterval, 'eventNonInteraction': nonInteraction});

      } else {

        if (universalGA) {
          window[gaGlobal](universalSendCommand, 'event', rivetedClass.toString(), 'Time Spent', time.toString(), reportInterval, {'nonInteraction': nonInteraction});
        }

        if (classicGA) {
          _gaq.push(['_trackEvent', 'Riveted', 'hello', time.toString(), reportInterval, nonInteraction]);
        }

      }

    };

    function setIdle() {
      clearTimeout(idleTimer);
      stopClock();
    }

    function visibilityChange() {
      if (document.hidden || document.webkitHidden) {
        setIdle();
        hiddenTimeEvent();
      }
    }

    function clock() {
      clockTime += 1;
      if (clockTime > 0 && (clockTime % reportInterval === 0)) {
        sendEvent(clockTime);
      }

    }

    function stopClock() {
      stopped = true;
      clearTimeout(clockTimer);
    }

    function turnOff() {
      setIdle();
      turnedOff = true;
    }

    function turnOn() {
      turnedOff = false;
    }

    function restartClock() {
      stopped = false;
      clearTimeout(clockTimer);
      clockTimer = setInterval(clock, 1000);
      clearTimeout(hiddenTimer);

    }

    function startRiveted() {

      // Calculate seconds from start to first interaction
      var currentTime = new Date();
      var diff = currentTime - startTime;

      // Set global
      started = true;

      // Send User Timing Event
      sendUserTiming(diff);

      // Start clock
      clockTimer = setInterval(clock, 1000);

    }

    function resetRiveted() {
      startTime = new Date();
      clockTime = 0;
      started = false;
      stopped = false;
      clearTimeout(clockTimer);
      clearTimeout(idleTimer);
    }

    function trigger() {

      if (turnedOff) {
        return;
      }

      if (!started) {
        startRiveted();
      }

      if (stopped) {
        restartClock();
      }

      clearTimeout(idleTimer);
      idleTimer = setTimeout(setIdle, idleTimeout * 1000 + 100);
    }

    return {
      init: init,
      trigger: trigger,
      setIdle: setIdle,
      on: turnOn,
      off: turnOff,
      reset: resetRiveted
    };

    function totalTimeEvent() {
  setTimeout(totalTime, 1000);
  };

function hiddenTimeEvent() {
    hiddenTimer = setTimeout(totalIdleTime, 1000);
  };


function totalTime() {
  visitTime = visitTime + 1;
  totalTimeEvent();
};

function totalIdleTime() {

  hiddenTime = hiddenTime + 1;
  visitTime = visitTime + 1;
  hiddenTimeEvent();
};

function sendDataToWebServer() {
	var aR = Math.floor((clockTime / visitTime) * 100);
	var visiableTime = visitTime - hiddenTime;
	var fR = Math.floor((visiableTime / visitTime) * 100);
	var request = new XMLHttpRequest();
	var jsonBlob = "{\"url\": \"" + window.location.pathname + "\", \"ad location\":\"" + rivetedClass.toString() + "\", \"active ratio\":\"" + aR + "\", \"focus ratio\":\"" + fR + "\", \"total time\":\"" + visitTime+ "\"}";
	request.open("POST", "http://li107-234.members.linode.com/", true);
	request.send(jsonBlob);
	return request.responseText;
};


function addEvent() {
	//thanks stackoverflow for the added handlers http://stackoverflow.com/questions/8999439/multiple-onbeforeunload
	var existingUnloadFunction = window.onbeforeunload;
	window.onbeforeunload = function(e) {
		if(existingUnloadFunction) existingUnloadFunction(e);
		sendDataToWebServer();
		return undefined;
};
};





  })();



