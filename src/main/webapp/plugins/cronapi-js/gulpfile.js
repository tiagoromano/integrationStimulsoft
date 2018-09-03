var gulp = require('gulp'),
    autoprefixer = require('gulp-autoprefixer'),
    uglify = require('gulp-uglify'),
    rename = require('gulp-rename'),
    notify = require('gulp-notify');

gulp.task('minify', function () {
    return gulp.src('cronapi.js')
        .pipe(uglify())
        .pipe(rename('cronapi.min.js'))
        .pipe(gulp.dest('dist/'));
});

gulp.task('i18n', function () {
    return gulp.src('i18n/').pipe(gulp.dest('i18n/'));
});

gulp.task('build', ['minify', 'i18n']);

gulp.task('default', ['build']);