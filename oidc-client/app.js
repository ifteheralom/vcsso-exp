'use strict';

import express from 'express';
import { Issuer, Strategy } from 'openid-client';
import passport from 'passport';
import expressSession from 'express-session';
import { engine } from 'express-handlebars';
import {
    generateRandomStrings,
    convertObjectToArrayWithKeys,
    measureExecutionTime
} from "./utils.js"


const app = express();

app.engine('hbs', engine({
    extname:'hbs', defaultLayout:'layout.hbs'
}));
app.set('view engine', 'hbs');

const keycloakIssuer = await Issuer.discover('http://localhost:8080/realms/vcsso')
//console.log('Discovered issuer %s %O', keycloakIssuer.issuer, keycloakIssuer.metadata);

const client = new keycloakIssuer.Client({
    client_id: 'vcsso-openid',
    client_secret: 'long_secret-here',
    redirect_uris: ['http://localhost:3000/auth/callback'],
    post_logout_redirect_uris: ['http://localhost:3000/logout/callback'],
    response_types: ['code'],
  });

var memoryStore = new expressSession.MemoryStore();
app.use(
    expressSession({
    secret: 'another_long_secret',
    resave: false,
    saveUninitialized: true,
    store: memoryStore
    })
);

app.use(passport.initialize());
app.use(passport.authenticate('session'));

passport.use('oidc', new Strategy({client}, (tokenSet, userinfo, done)=>{
    return done(null, tokenSet.claims());
}))

passport.serializeUser(function(user, done) {
    done(null, user);
});
passport.deserializeUser(function(user, done) {
    done(null, user);
});

app.get('/test', (req, res, next) => {
    const start = performance.now();

    passport.authenticate('oidc')(req, res, next);
    
    const end = performance.now();
    const timeTaken = end - start;
    console.log('[OIDC Auth]', timeTaken)
});

app.get('/auth/callback', (req, res, next) => {
    passport.authenticate('oidc', {
      successRedirect: '/testauth',
      failureRedirect: '/'
    })(req, res, next);
});

var checkAuthenticated = (req, res, next) => {
    if (req.isAuthenticated()) { 
        return next() 
    }
    res.redirect("/test")
}

app.get('/testauth', checkAuthenticated, (req, res) => {
    res.render('test');
});

app.get('/other', checkAuthenticated, (req, res) => {
    res.render('other');
});

app.get('/',function(req,res){
    res.render('index');
});

app.get('/logout', (req, res) => {
    res.redirect(client.endSessionUrl());
});

app.get('/logout/callback', (req, res) => {
    //req.logout();
    res.redirect('/');
});

app.listen(3000, function () {
    console.log('Listening at http://localhost:3000');
});
